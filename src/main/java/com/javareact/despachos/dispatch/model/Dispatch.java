package com.javareact.despachos.dispatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Mapeo R2DBC para la tabla 'despacho' en PostgreSQL.
 */
@Table("despacho")
@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class Dispatch {

    @Id
    private Long id;

    @Column("cliente_id")
    private Long customerId;

    @Column("ciudad")
    private String city;

    @Column("estado")
    private String status;

    @Column("tarifa")
    private Double fare;

    @Column("score_riesgo")
    private Integer riskScore;

    @Column("expira_en")
    private Instant expiresAt;

    @Column("creado_en")
    private Instant createdAt;
}