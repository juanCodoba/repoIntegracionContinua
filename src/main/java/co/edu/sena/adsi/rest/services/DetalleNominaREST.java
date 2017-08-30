
package co.edu.sena.adsi.rest.services;

import co.edu.sena.adsi.jpa.entities.DetalleNomina;
import co.edu.sena.adsi.jpa.entities.Nomina;
import co.edu.sena.adsi.jpa.entities.Usuario;
import co.edu.sena.adsi.jpa.entities.Usuario_;
import co.edu.sena.adsi.jpa.sessions.DetalleNominaFacade;
import co.edu.sena.adsi.jpa.sessions.NominaFacade;
import co.edu.sena.adsi.jpa.sessions.UsuarioFacade;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author rootbean
 */
@Path("detalle_nominas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DetalleNominaREST {
    
    @EJB
    private DetalleNominaFacade detalleNominaEJB;
    
    @EJB
    private UsuarioFacade  usuarioEJB;
    
    @EJB 
    private NominaFacade nominaEJB;
    
    
    @GET
    public List<DetalleNomina> findAll(){
        return detalleNominaEJB.findAll();
    }
    
    @GET
    @Path("{id}")
    public DetalleNomina findById(@PathParam("id") Integer id){
        return detalleNominaEJB.find(id);
    }
    
    @POST
    public void create(@QueryParam("idUsuario") Integer idUsuario,
            @QueryParam("diasLaborados") int diasLaborados ,
            @QueryParam("idNomina") Integer idNomina
    ){
        
       try {
            DetalleNomina  newDetallenomina = new DetalleNomina();
        
        //CONSULTANDO EL ID DEL USUARIO 
        Usuario empleado = usuarioEJB.find(idUsuario);
        
        //CONSULTADO EL ID DE LA NOMINA 
        Nomina nomina = nominaEJB.find(idNomina);
        
        newDetallenomina.setDiasLaborados(diasLaborados);
        
        //sueldo de vengado sueldobasico/30*diaslaborados
        newDetallenomina.setSueldoDevengado(empleado.getSueldoBasico() /30 * diasLaborados);
        
        //auxilo trasporte 
        newDetallenomina.setAuxilioTransporte(obtenerAuxTransporte(empleado.getSueldoBasico(),diasLaborados ));
        
        //Horas extras sueldo de vengado * 10%  
        newDetallenomina.setValorHorasExtras(newDetallenomina.getSueldoDevengado() * 0.1);
        
        //toatal de vengado =sueldoDevegado + auxilioTrasporte + HorasExtras
        newDetallenomina.setTotalDescuento(newDetallenomina.getSueldoDevengado() +
                newDetallenomina.getAuxilioTransporte() +
                newDetallenomina.getValorHorasExtras()
        );
        //descuento Salud = totalDevengado-auxiloTransporte * 0.04%
        newDetallenomina.setDescuentoSalud(newDetallenomina.getTotalDevengado()-
                newDetallenomina.getAuxilioTransporte() * 0.04);
        
        //descuento Pension = totalDevengado-auxiloTransporte * 0.04%
        newDetallenomina.setDescuentoPension(newDetallenomina.getTotalDevengado()-
                newDetallenomina.getAuxilioTransporte() * 0.04);
        
        //otros descuentos = 300000
        newDetallenomina.setOtrosDescuentos(300000);
        
        //descuento por fondo de solidaridad
        
        newDetallenomina.setDescuentoFondoSolidaridad(DescuentoSolidaridad(newDetallenomina.getTotalDevengado(),empleado.getSueldoBasico()));
        
        //totalDescuento = sumas total de todos los cuentos 
        newDetallenomina.setTotalDescuento(newDetallenomina.getDescuentoSalud()+
                newDetallenomina.getDescuentoPension() + 
                newDetallenomina.getDescuentoFondoSolidaridad());
        
        //total neto a pagar
        newDetallenomina.setNetoPagar(newDetallenomina.getTotalDevengado()-newDetallenomina.getTotalDescuento());
        
        newDetallenomina.setEmpleado(empleado);
        newDetallenomina.setNomina(new Nomina (idNomina));
        
        detalleNominaEJB.create(newDetallenomina);
        } catch (Exception e) {
            System.out.println("error"  +  e);
            
        }
       
    }
                
    
    
    private double obtenerAuxTransporte(double sueldoBasico,int diasLaborados){
         double auxTransporte = 0;
        
         if (sueldoBasico <= 737737 * 2) {
            
            auxTransporte = 83500 * 30 /diasLaborados;
            
            
        }else{
            
        }
        return auxTransporte;
    }
     private double DescuentoSolidaridad(double totalDevengado,double sueldoBasico){
       double fondoSolidaridad = 0;
       
         if (sueldoBasico >= 737737 * 4) {
             fondoSolidaridad = totalDevengado * 0.01;
             
         }
       
       
       return fondoSolidaridad;
     }
}