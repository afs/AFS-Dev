/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package projects.prefixes;



public class XYZ
{
    /*
     * 
     * 
     * DatasetPrefixes to return PrefixMapStorage
     * TestPrefixMappingOverPrefixMap1 - over DatasetPrefixes dft and named
     * 
     * 
     * 
Prefixes:

PrefixMap as interface? Maybe not - split out storage.
PrefixMapFactory?

PrefixMapStorage<String, String>
  Put,get,putall,getall, clear, sync, close

PrefixMap<String, IRI> over a PrefixMapStorage
  add, abbreviate, contains, getMapping, expand
  canonicalization

Storage abstraction:

DatasetPrefixStorage
  PrefixMapStorageView as projection of DatasetPrefixStorage

  but to PrefixMap
and PrefixMap is an interface.

PrefixMapping 2 PrefixMap
PrefixMap with cache over
  PrefixMapStorage 
     */

    
}

