/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
 
package projects.dsg2.storage;

public class StorageRDF_Switching extends StorageRDF_Wrapper {
    
    // Or settable StorageRDF_Wrapper

    private StorageRDF other1;
    private StorageRDF other2;
    private StorageRDF current;

    protected StorageRDF_Switching(StorageRDF other1, StorageRDF other2) {
        super(null);
        this.other1 = other1 ;
        this.other2 = other2 ;
    }
    
    @Override
    protected StorageRDF get() { return current ; }
    
    // 
}
