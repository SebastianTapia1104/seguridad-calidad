package com.duoc.backend.controllers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.duoc.backend.entities.DetalleFactura;
import com.duoc.backend.entities.Factura;
import com.duoc.backend.entities.Visita;
import com.duoc.backend.repositories.FacturaRepository;
import com.duoc.backend.repositories.VisitaRepository;
import com.duoc.backend.services.FacturaMailService;
import com.duoc.backend.services.FacturaPdfService;

@Controller
@RequestMapping("/admin/facturacion")
@PreAuthorize("hasRole('ADMIN')")
public class FacturaController {

    private final VisitaRepository visitaRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaPdfService facturaPdfService;
    private final FacturaMailService facturaMailService;

    public FacturaController(
            VisitaRepository visitaRepository,
            FacturaRepository facturaRepository,
            FacturaPdfService facturaPdfService,
            FacturaMailService facturaMailService) {
        this.visitaRepository = visitaRepository;
        this.facturaRepository = facturaRepository;
        this.facturaPdfService = facturaPdfService;
        this.facturaMailService = facturaMailService;
    }

    @GetMapping
    public String index(Model model) {
        // Obtener visitas sin factura
        List<Visita> visitasSinFactura = visitaRepository.findAll().stream()
                .filter(v -> !facturaRepository.existsByVisita_Id(v.getId()))
                .toList();
        
        model.addAttribute("visitas", visitasSinFactura);
        model.addAttribute("facturas", facturaRepository.findAll());
        return "admin/facturacion";
    }

    @PostMapping("/generar")
    public String generarFactura(
            @RequestParam Long visitaId,
            @RequestParam List<String> conceptos,
            @RequestParam List<BigDecimal> montos,
            @RequestParam(required = false) boolean enviarEmail,
            RedirectAttributes redirectAttrs) {
        
        try {
            Visita visita = visitaRepository.findById(visitaId)
                    .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada"));
            
            if (facturaRepository.existsByVisita_Id(visitaId)) {
                redirectAttrs.addFlashAttribute("error", "Ya existe una factura para esta visita");
                return "redirect:/admin/facturacion";
            }
            
            // Calcular total
            BigDecimal total = montos.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Crear factura
            Factura factura = new Factura(visita, total, Instant.now());
            
            // Agregar detalles
            for (int i = 0; i < conceptos.size(); i++) {
                if (conceptos.get(i) != null && !conceptos.get(i).isBlank() && montos.get(i) != null) {
                    factura.agregarDetalle(new DetalleFactura(conceptos.get(i), montos.get(i)));
                }
            }
            
            factura = facturaRepository.save(factura);
            
            // Generar PDF
            byte[] pdf = facturaPdfService.generarPdf(factura);
            
            // Enviar email si se solicitó
            if (enviarEmail) {
                facturaMailService.enviarFacturaPdf(factura, pdf);
                redirectAttrs.addFlashAttribute("msg", "Factura generada y enviada por email correctamente");
            } else {
                redirectAttrs.addFlashAttribute("msg", "Factura generada correctamente");
            }
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al generar factura: " + e.getMessage());
        }
        
        return "redirect:/admin/facturacion";
    }
}
