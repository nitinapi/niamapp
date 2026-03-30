package com.niamapp;

import com.niamapp.proto.NiamProto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NiamController.class)
class NiamControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getNames_returns200() throws Exception {
        mockMvc.perform(get("/api/names").accept("application/x-protobuf"))
                .andExpect(status().isOk());
    }

    @Test
    void getNames_returnsProtobufContentType() throws Exception {
        mockMvc.perform(get("/api/names").accept("application/x-protobuf"))
                .andExpect(content().contentType("application/x-protobuf"));
    }

    @Test
    void getNames_bodyDecodesCorrectly() throws Exception {
        byte[] body = mockMvc.perform(get("/api/names").accept("application/x-protobuf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        NiamProto.NamesResponse response = NiamProto.NamesResponse.parseFrom(body);
        List<String> names = response.getNamesList();

        assertThat(names).containsExactly("Niam", "Mani", "Anim", "Main", "Amin");
    }

    @Test
    void getNames_firstNameIsNiam() throws Exception {
        byte[] body = mockMvc.perform(get("/api/names").accept("application/x-protobuf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        NiamProto.NamesResponse response = NiamProto.NamesResponse.parseFrom(body);

        assertThat(response.getNames(0)).isEqualTo("Niam");
    }

    @Test
    void getNames_returnsExactlyFiveNames() throws Exception {
        byte[] body = mockMvc.perform(get("/api/names").accept("application/x-protobuf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        NiamProto.NamesResponse response = NiamProto.NamesResponse.parseFrom(body);

        assertThat(response.getNamesCount()).isEqualTo(5);
    }
}
