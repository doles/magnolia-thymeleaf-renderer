
/*
 * Copyright (c) 2014 Thomas Kratz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package thymeleaf.dialect;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;
import thymeleaf.processor.CmsAreaElementProcessor;
import thymeleaf.processor.CmsComponentElementProcessor;
import thymeleaf.processor.CmsInitElementProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10.11.12
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class MagnoliaDialect extends AbstractDialect {


    @Override
    public String getPrefix() {
        return "cms";
    }



    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new CmsInitElementProcessor());
        processors.add(new CmsAreaElementProcessor());
        processors.add(new CmsComponentElementProcessor());
        return processors;
    }


}
