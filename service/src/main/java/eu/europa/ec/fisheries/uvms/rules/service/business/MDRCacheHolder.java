/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.rules.service.business;

import eu.europa.ec.fisheries.uvms.rules.service.constants.MDRAcronymType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sanera on 20/06/2017.
 */
public class MDRCacheHolder {

    private Map<MDRAcronymType, List<String>> cache =new ConcurrentHashMap<>();

    private static class Holder {
        static final MDRCacheHolder INSTANCE = new MDRCacheHolder();
    }

    public static MDRCacheHolder getInstance() {
        return Holder.INSTANCE;
    }

    public void addToCache(MDRAcronymType type, List<String> values){
         synchronized (cache){
             cache.put(type,values);
         }
    }

    public List<String> getList(MDRAcronymType type){
        List<String> values;
        synchronized (cache){
            values=   cache.get(type);
        }
        return values;
    }

}
