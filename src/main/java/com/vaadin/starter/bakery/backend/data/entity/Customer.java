package com.vaadin.starter.bakery.backend.data.entity;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Entidade que representa um cliente no sistema de padaria.
 * <p>
 * Contém informações básicas do cliente como nome completo, telefone e detalhes adicionais.
 * </p>
 * 
 * @author LETI
 */
@Entity
public class Customer extends AbstractEntity {

    /**
     * Nome completo do cliente.
     * <p>
     * Campo obrigatório, limitado a 255 caracteres.
     * </p>
     */
    @NotBlank
    @Size(max = 255)
    private String fullName;

    /**
     * Número de telefone do cliente.
     * <p>
     * Campo obrigatório*

