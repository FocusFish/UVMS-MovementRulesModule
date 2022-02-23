/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.movementrules.rest.service.dto;

import org.junit.Test;
import fish.focus.uvms.movementrules.service.dto.ResponseCode;
import fish.focus.uvms.movementrules.service.dto.ResponseDto;
import static org.junit.Assert.assertEquals;

public class ResponseTest {

    public ResponseTest() {
    }

    @Test
    public void checkDtoReturnsValid() {
        String VALUE = "HELLO_DTO";
        ResponseDto dto = new ResponseDto<>(VALUE, ResponseCode.OK);
        assertEquals(dto.getCode().intValue(), ResponseCode.OK.getCode());
        assertEquals(VALUE, dto.getData());

        dto = new ResponseDto<>(VALUE, ResponseCode.UNDEFINED_ERROR);
        assertEquals(dto.getCode().intValue(), ResponseCode.UNDEFINED_ERROR.getCode());
        assertEquals(VALUE, dto.getData());
    }
}
