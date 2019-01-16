/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
import { ContainerModule } from 'inversify';
import { LanguageServerContribution } from '@theia/languages/lib/node';
import { SpringBootLsContribution } from './spring-boot-ls-contribution';
import { BootJavaExtension } from './java-extension';
import { JavaExtensionContribution } from '@theia/java/lib/node';

export default new ContainerModule(bind => {
    bind(LanguageServerContribution).to(SpringBootLsContribution).inSingletonScope();
    bind(JavaExtensionContribution).to(BootJavaExtension).inSingletonScope();
});