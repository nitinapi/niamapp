package com.niamapp;

import com.niamapp.proto.NiamProto;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NiamController {

    private final List<String> names = List.of("Niam", "Mani", "Anim", "Main", "Amin");

    @GetMapping(value = "/names", produces = "application/x-protobuf")
    public byte[] getNames() {
        NiamProto.NamesResponse response = NiamProto.NamesResponse.newBuilder()
                .addAllNames(names)
                .build();
        return response.toByteArray();
    }
}
