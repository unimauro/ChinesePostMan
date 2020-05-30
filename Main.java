/*
 * Main.java
 *
 * Created on 22 de junio de 2007, 07:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 *
 */

package cartero;

import java.io.*;
import java.util.*;

/**
 * @author unimauro
 */
public class Main {
    
    int N; // numero de nodo 
	int delta[]; // nodo de los deltas
	int neg[], pos[]; // nodo desbalancedos
	int arcs[][]; // matriz de arcos entre ambos nodo
	Vector etiqueta[][]; // etiquetas de los vectores y arcos (para cada vertice par) 
	int f[][]; // repitiendo los arcos
	float c[][]; // costo minimos de los arcos y sus direccionesl; rutas
	String etiquetaMinima[][]; // etiquetas de arcos de costo minimo
	boolean definido[][]; // Costo de ruta definida entre ambos nodo
	int ruta[][]; // arbol grafico
	float costoBasico; // costo total 
               
    
        	void solucion()
	{	minimaRutaCosto();
		verificandoValidez();
		encontrandoNoBalanceado();
		encontrandoFactibles();
		while( mejoras() );
	}

        
    /** Creates a new instance of Main */
    public Main(int nodo) 	
		{	
                if( (N = nodo) <= 0 ) throw new Error("El grafico no existe");
		delta = new int[N];
		definido = new boolean[N][N];
		etiqueta = new Vector[N][N];
		c = new float[N][N];
		f = new int[N][N];
		arcs = new int[N][N];
		etiquetaMinima = new String[N][N];
		ruta = new int[N][N];
		costoBasico = 0;
	}
                
        
	// agregar Trayectos
	Main addArc(String eti, int u, int v, float costo)
	{	if( !definido[u][v] ) etiqueta[u][v] = new Vector();
		etiqueta[u][v].addElement(eti); 
		costoBasico += costo;
		if( !definido[u][v] || c[u][v] > costo )
		{	c[u][v] = costo;
			etiquetaMinima[u][v] = eti;
			definido[u][v] = true;
			ruta[u][v] = v;
		}
		arcs[u][v]++;
		delta[u]++;
		delta[v]--;
		return this;
	}
	//
	
	/** Algoritmo de Floyd
	 *  
	 *  
	 */
	// 
        
	void minimaRutaCosto()
	{	for( int k = 0; k < N; k++ )
			for( int i = 0; i < N; i++ )
				if( definido[i][k] )
					for( int j = 0; j < N; j++ )
						if( definido[k][j]
						    && (!definido[i][j] || c[i][j] > c[i][k]+c[k][j]) )
						{	ruta[i][j] = ruta[i][k];
							c[i][j] = c[i][k]+c[k][j];
							definido[i][j] = true;
							if( i == j && c[i][j] < 0 ) return; 
						}
	}

	//Validando
	void verificandoValidez()
	{	for( int i = 0; i < N; i++ )
		{	for( int j = 0; j < N; j++ )
				if( !definido[i][j] ) throw new Error("El grafico no es correcto");
			if( c[i][i] < 0 ) throw new Error("El grafico tiene ciclo negativo");
		}
	}

    // Costo
	float costo()
	{	return costoBasico+phi();
	}
	
	float phi()
	{	float phi = 0;
		for( int i = 0; i < N; i++ )
			for( int j = 0; j < N; j++ )
				phi += c[i][j]*f[i][j];
		return phi;
	}
    
    //Encontrando no balanceados
        
    
	void encontrandoNoBalanceado()
	{	int nn = 0, np = 0; // numero de nodo positivos y negativos de los deltas
		
		for( int i = 0; i < N; i++ )
			if( delta[i] < 0 ) nn++;
			else if( delta[i] > 0 ) np++;
			
		neg = new int[nn];
		pos = new int[np];
		nn = np = 0;
		for( int i = 0; i < N; i++ ) // inciando pasos
			if( delta[i] < 0 ) neg[nn++] = i;
			else if( delta[i] > 0 ) pos[np++] = i;
	}
        
   //Encontrando rutas factibles
 	
	void encontrandoFactibles()
	{	
		int delta[] = new int[N];
		for( int i = 0; i < N; i++ )
			delta[i] = this.delta[i];
		
		for( int u = 0; u < neg.length; u++ )
		{	int i = neg[u];
			for( int v = 0; v < pos.length; v++ )
			{	int j = pos[v];
				f[i][j] = -delta[i] < delta[j]? -delta[i]: delta[j];
				delta[i] += f[i][j];
				delta[j] -= f[i][j];
			}
		}
	}
	
	// Haciendo Mejoras
	boolean mejoras()
	{	Main residual = new Main(N); 
		for( int u = 0; u < neg.length; u++ )
		{	int i = neg[u];
			for( int v = 0; v < pos.length; v++ )
			{	int j = pos[v];
				residual.addArc(null, i, j, c[i][j]);
				if( f[i][j] != 0 ) residual.addArc(null, j, i, -c[i][j]);
			}
		}	
		residual.minimaRutaCosto(); // encontrando un ciclo negativo
		for( int i = 0; i < N; i++ ) 
			if( residual.c[i][i] < 0 ) // cancelando un ciclo o alguno 
			{	int k = 0, u, v; 
				boolean kunset = true;
				u = i; do // encontrando un k
				{	v = residual.ruta[u][i];
					if( residual.c[u][v] < 0 && (kunset || k > f[v][u]) ) 
					{	k = f[v][u];
						kunset = false;
					}
				} while( (u = v) != i );
				u = i; do // cancelando un ciclo de vida
				{	v = residual.ruta[u][i];
					if( residual.c[u][v] < 0 ) f[v][u] -= k;
					else f[u][v] += k;
				} while( (u = v) != i );
				return true; //
			}
		return false; // no hay soluciones
	}
	
	// Imprimir 
	static final int NONE = -1; // alguno < 0
	
	int encontrandoRuta(int from, int f[][]) // encontrando algo desbalanceado
	{	for( int i = 0; i < N; i++ )
			if( f[from][i] > 0 ) return i;
		return NONE; 
	}
	
	void imprimiendoCartero(int verticeInicio)
	{	int v = verticeInicio;
		
		// Borrando esto se hace rapido pero a la vez impreciso :D 
		int arcs[][] = new int[N][N];
		int f[][] = new int[N][N];
		for( int i = 0; i < N; i++ )
			for( int j = 0; j < N; j++ )
			{	arcs[i][j] = this.arcs[i][j];
				f[i][j] = this.f[i][j];
			}
		
		while( true )
		{	int u = v;
			if( (v = encontrandoRuta(u, f)) != NONE )
			{	f[u][v]--; // removiendo las direcciones
				for( int p; u != v; u = p ) // Rompiendo 
				{	p = ruta[u][v];
					System.out.println("Tomando el arco "+etiquetaMinima[u][p]
						+" desde "+u+" a "+p);
				}
			}	
			else
			{	int nodoPuente = ruta[u][verticeInicio];
				if( arcs[u][nodoPuente] == 0 )
					break; // hallar un arco
				v = nodoPuente;
				for( int i = 0; i < N; i++ ) // hallar un arco usado 
					if( i != nodoPuente && arcs[u][i] > 0 )
					{	v = i;
						break;
					}
				arcs[u][v]--; // decrementando cuenta de arcos paralelos
				System.out.println("Tomando el arco "+etiqueta[u][v].elementAt(arcs[u][v])
					+" desde "+u+" a "+v); // uso de cada etiqueta de arco
			}
		}
	}
//
	
	static public void main(String args[]) throws IOException
	{	
		IniciandoCartero.test();
	}

}



/*
 *
 *Creando una Clase para implementar el algoritmo del Cartero
 *
 */
class IniciandoCartero 
{	class Arc 
	{	String eti; int u, v; float costo;
		Arc(String eti, int u, int v, float costo) // Definiendo los Arcos
		{	this.eti = eti;  // etiqueta un String
			this.u = u; // Nodo Inicial
			this.v = v; // Nodo Final
			this.costo = costo; // Costo del Arco
		}
	}
        
	Vector arcs = new Vector();
	int N;
	
	IniciandoCartero(int nodo)
	{	N = nodo;
	}
	
	IniciandoCartero addArc(String eti, int u, int v, float costo)
	{	if( costo < 0 ) throw new Error("Grafico que tiene costo negativo");
		arcs.addElement(new Arc(eti, u, v, costo));
		return this;
	}
	
	float imprimiendoCartero(int verticeInicio)
	{	Main mejorGrafico = null, g;
		float mejorCosto = 0, costo;
		int i = 0;
		do
		{	g = new Main(N+1);
			for( int j = 0; j < arcs.size(); j++ )
			{	Arc it = (Arc) arcs.elementAt(j);
				g.addArc(it.eti, it.u, it.v, it.costo);
			}
			costo = g.costoBasico;
			g.encontrandoNoBalanceado(); // Inicializa g.neg en un grafico original
			g.addArc("'inicio virtual'", N, verticeInicio, costo);
			g.addArc("'fin virtual'", 
				// Grafico Euleriano si neg.length=0
				g.neg.length == 0? verticeInicio: g.neg[i], N, costo);
			g.solucion();
			if( mejorGrafico == null || mejorCosto > g.costo() )
			{	mejorCosto = g.costo();
				mejorGrafico = g;
			}
		} while( ++i < g.neg.length );
		System.out.println("Iniciando el Algoritmo para "+verticeInicio+" (ignoro el arco virutal)");
		mejorGrafico.imprimiendoCartero(N);
		return costo+mejorGrafico.phi();
	}
//
	
	static void test()throws IOException
	{
	                
                InputStreamReader isr=new InputStreamReader(System.in);
        		//Creación del filtro para optimizar la lectura de datos
        		BufferedReader br=new BufferedReader(isr);
        		System.out.print("Introduce el Numero de Nodos: ");
        		//Lectura de datos mediante el método readLine()
        		String texto1=br.readLine();
        		//Conversión a int de la String anterior para poder sumar
        		int N=Integer.parseInt(texto1);                
                IniciandoCartero G = new IniciandoCartero(N); 
                
                // crea un grafico de 10 nodo
                /* agrega arcos al grafico de ejemplo
                 *
                 *("a", 0, 1, 1)  === *(arco, nodoi, nodof, peso) 
                 *Donde :
                 *a == arco : Nombre del Arco
                 *0 == nodoi : Nombre del Nodo Inicial
                 *1 == nodof : Nombre del Nodo Final
                 *1 == peso : El Peso del Arco
                 *
                 */
                
                InputStreamReader ar=new InputStreamReader(System.in);
        		BufferedReader arv=new BufferedReader(ar);
        		System.out.print("Introduce el Numero de Arcos: ");
        		String ar1=arv.readLine();
        		int ar2=Integer.parseInt(ar1);
        		
        		for ( int i=0;i<ar2; i++){
        			InputStreamReader ni=new InputStreamReader(System.in);
            		BufferedReader nir=new BufferedReader(ni);
            		BufferedReader nir1=new BufferedReader(ni);
            		BufferedReader nir2=new BufferedReader(ni);
            		System.out.println(" Arco: "+ i +" Nodo Inicio, Nodo Fin, Costo: ");
            		String nir0c=nir.readLine();
            		int nir0=Integer.parseInt(nir0c);
            		String nfrc=nir1.readLine();
            		int nfr=Integer.parseInt(nfrc);
            		String costoc=nir2.readLine();
            		int costo=Integer.parseInt(costoc);
            		//int niri=Integer.parseInt(nir1);
            		System.out.print(nir0+" "+nfr+" "+costo);
            		G.addArc(""+i+"", nir0, nfr, costo);        			
        		}
        		
        		
                /*
                 * G.addArc("a", 0, 1, 1).addArc("b", 0, 2, 1).addArc("c", 1, 2, 1)
		 		.addArc("d", 1, 3, 1).addArc("e", 2, 3, 1).addArc("f", 3, 0, 1);
                */
                
                /*
                 *Solicitar el numero de Arcos
                 *  int Arc
                 * System.in(numero de Arcos, Arc)
                 */
                /*
                for (int i=0, i <Arc; i++ )
                {    
                {System.print(Arco i,   )
                 System.in(G.addArc(eti,))}
                }
                */
                /*
                G.addArc("1", 0, 1, 1).addArc("2", 1, 2, 3).addArc("3", 2, 3, 1)
		 .addArc("4", 3, 4, 3).addArc("5", 4, 5, 4).addArc("6", 5, 2, 6)
                 .addArc("7", 4, 6, 6).addArc("8", 6, 7, 9).addArc("9", 7, 8, 4)
                 .addArc("10", 8, 9, 1).addArc("11", 9, 7, 5).addArc("12", 8, 1, 4)
                 .addArc("13", 9, 0, 2);
                */
                
                
                
		int mejori = 0;
		float mejorCosto = 0;
		for( int i = 0; i < 4; i++ )
		{	System.out.println("Solucion de "+i);
			float c = G.imprimiendoCartero(i);
			System.out.println("Costo = "+c);
			if( i == 0 || c < mejorCosto )
			{	mejorCosto = c;
				mejori = i;
			}
		}
		System.out.println("Iniciando....");
		G.imprimiendoCartero(mejori);
		System.out.println("El Menor Costo = "+mejorCosto+"" +
                            "\n=====================");
		
	}

}
