/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.boot.java.SpringAotJavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.BeanPostProcessingIgnoreInAot;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class BeanPostProcessingIgnoreInAotReconciler implements JdtAstReconciler {
	
	private static final String RECIPE_ID = BeanPostProcessingIgnoreInAot.class.getName();
	private static final String LABEL = "Add method 'isBeanExcludedFromAotProcessing' that returns 'false'";
    private static final String RUNTIME_BEAN_POST_PROCESSOR = "org.springframework.beans.factory.config.BeanPostProcessor";
    private static final String COMPILE_BEAN_POST_PROCESSOR = "org.springframework.beans.factory.aot.BeanRegistrationAotProcessor";
    public static final String METHOD_NAME = "isBeanExcludedFromAotProcessing";
	
	private QuickfixRegistry registry;

	public BeanPostProcessingIgnoreInAotReconciler(QuickfixRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void reconcile(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector,
			boolean isCompleteAst) throws RequiredCompleteAstException {
		AtomicBoolean requiresFullAst = new AtomicBoolean(false);
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration typeDecl) {
				ITypeBinding type = typeDecl.resolveBinding();
				if (type != null && isApplicable(type)) {
					MethodDeclaration foundMethod = null;
					boolean markProblem = false;
					for (MethodDeclaration m : typeDecl.getMethods()) {
						if (METHOD_NAME.equals(m.getName().getIdentifier()) && m.parameters().isEmpty()) {
							foundMethod = m;
							break;
						}
					}
					if (foundMethod != null) {
						if (isCompleteAst) {
							AtomicBoolean returnsTrue = new AtomicBoolean(false);
							foundMethod.accept(new ASTVisitor() {
								@Override
								public boolean visit(ReturnStatement node) {
									if (Boolean.TRUE.equals(node.getExpression().resolveConstantExpressionValue())) {
										returnsTrue.set(true);
									}
									return !returnsTrue.get();
								}
							});
							markProblem = returnsTrue.get();
						} else {
							requiresFullAst.set(true); 
						}
					} else {
						markProblem = true;
					}
					
					if (markProblem) {
						ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(), LABEL, typeDecl.getName().getStartPosition(), typeDecl.getName().getLength());
						RewriteQuickFixUtils.setRewriteFixes(registry, problem, List.of(
								new FixDescriptor(RECIPE_ID, List.of(docUri.toASCIIString()), RewriteQuickFixUtils.buildLabel(LABEL, RecipeScope.NODE))
										.withRangeScope(RewriteQuickFixUtils.createOpenRewriteRange(cu, typeDecl))
										.withRecipeScope(RecipeScope.NODE)
						));
						problemCollector.accept(problem);
					}
				}
				return !requiresFullAst.get();
			}
			
			private boolean isApplicable(ITypeBinding type) {
				return RewriteQuickFixUtils.implementsType(RUNTIME_BEAN_POST_PROCESSOR, type) && RewriteQuickFixUtils.implementsType(COMPILE_BEAN_POST_PROCESSOR, type);
			}
			
		});
		if (requiresFullAst.get()) {
			throw new RequiredCompleteAstException();
		}
	}
	
	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(3, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return SpringAotJavaProblemType.JAVA_BEAN_POST_PROCESSOR_IGNORED_IN_AOT;
	}

}
