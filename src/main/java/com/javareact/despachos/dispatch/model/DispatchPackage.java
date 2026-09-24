package com.javareact.despachos.dispatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("paquete")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class DispatchPackage {

    @Id
    private Long id;

    @Column("despacho_id")
    private Long dispatchId;

    @Column("vehiculo_id")
    private Long vehicleId;

    @Column("peso_kg")
    private Integer weightKg;
}
