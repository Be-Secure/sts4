/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.core.INameEnvironmentWithProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

import reactor.util.function.Tuple2;


public class AstParserTest {
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	
	private MavenJavaProject jp;

	@BeforeEach
	public void setup() throws Exception {
		jp =  projects.mavenProject("empty-boot-15-web-app");
		assertTrue(jp.getIndex().findType("org.springframework.boot.SpringApplication").exists());
	}

    @Test
    void test1() throws Exception {
        URL sourceUrl = SourceLinks.source(jp, "org.springframework.boot.SpringApplication").get();

        URI uri = sourceUrl.toURI();

        String unitName = "SpringApplication";

        char[] content = IOUtils.toString(uri).toCharArray();

        CompilationUnit cu = CompilationUnitCache.parse2(content, uri.toASCIIString(), unitName, jp);

        assertNotNull(cu);

        cu.accept(new ASTVisitor() {

            @Override
            public boolean visit(TypeDeclaration node) {
                ITypeBinding binding = node.resolveBinding();
                assertNotNull(binding);
                return super.visit(node);
            }

            @Override
            public boolean visit(SingleMemberAnnotation node) {
                IAnnotationBinding annotationBinding = node.resolveAnnotationBinding();
                assertNotNull(annotationBinding);
                ITypeBinding binding = node.resolveTypeBinding();
                assertNotNull(binding);
                return super.visit(node);
            }

            @Override
            public boolean visit(NormalAnnotation node) {
                IAnnotationBinding annotationBinding = node.resolveAnnotationBinding();
                assertNotNull(annotationBinding);
                ITypeBinding binding = node.resolveTypeBinding();
                assertNotNull(binding);
                return super.visit(node);
            }

            @Override
            public boolean visit(MarkerAnnotation node) {
                IAnnotationBinding annotationBinding = node.resolveAnnotationBinding();
                assertNotNull(annotationBinding);
                ITypeBinding binding = node.resolveTypeBinding();
                assertNotNull(binding);
                return super.visit(node);
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                IMethodBinding binding = node.resolveBinding();
                assertNotNull(binding);
                if (node.getReturnType2() != null) {
                    ITypeBinding returnTypeBinding = node.getReturnType2().resolveBinding();
                    assertNotNull(returnTypeBinding);
                }
                return super.visit(node);
            }

            @Override
            public boolean visit(FieldDeclaration node) {
                ITypeBinding binding = node.getType().resolveBinding();
                assertNotNull(binding);
                return super.visit(node);
            }

        });

    }
    
    @Test
    void testCuDeclaration() throws Exception {
        URL sourceUrl = SourceLinks.source(jp, "org.springframework.boot.SpringApplication").get();

        URI uri = sourceUrl.toURI();

        String unitName = "SpringApplication";

        char[] content = IOUtils.toString(uri).toCharArray();
        
		Tuple2<List<Classpath>, INameEnvironmentWithProgress> envTuple = CompilationUnitCache.createLookupEnvTuple(jp);
		

        CompilationUnitDeclaration cu = CompilationUnitCache.parse3(content, uri.toASCIIString(), unitName, envTuple.getT1(), envTuple.getT2());

        assertNotNull(cu);
        
        cu.traverse(new org.eclipse.jdt.internal.compiler.ASTVisitor() {

			@Override
			public boolean visit(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration,
					MethodScope scope) {
				// TODO Auto-generated method stub
				return super.visit(fieldDeclaration, scope);
			}
        	
        }, new CompilationUnitScope(cu, new CompilerOptions(CompilationUnitCache.createCompilerOptions())/*new LookupEnvironment(null, new CompilerOptions(CompilationUnitCache.createCompilerOptions()), null, envTuple.getT2())*/));
    }
    
}
