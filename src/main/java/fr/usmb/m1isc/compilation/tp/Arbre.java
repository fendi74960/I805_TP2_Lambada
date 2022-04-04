package fr.usmb.m1isc.compilation.tp;

import java.util.HashMap;

public class Arbre {
	private String racine;
	private Arbre arbreG;
	private Arbre arbreD;
	
	public String getRacine() {
		return racine;
	}
	public void setRacine(String racine) {
		this.racine = racine;
	}
	public Arbre getArbreG() {
		return arbreG;
	}
	public void setArbreG(Arbre arbreG) {
		this.arbreG = arbreG;
	}
	public Arbre getArbreD() {
		return arbreD;
	}
	public void setArbreD(Arbre arbreD) {
		this.arbreD = arbreD;
	}
	public Arbre(String racine, Arbre arbreG, Arbre arbreD) {
		super();
		this.racine = racine;
		this.arbreG = arbreG;
		this.arbreD = arbreD;
	}
	@Override
	public String toString() {
		if(getArbreG()==null && getArbreD()==null) {
			return racine;
		}
		else if(getArbreG()==null) {
			return "("+ racine +" "+getArbreD().toString() +")";
		}
		else if(getArbreD()==null) {
			return "("+ racine +" "+getArbreG().toString() +")";
		}
		if (racine!=null) {
			return "("+racine +" "+getArbreG().toString()+" "+getArbreD().toString() +")";
		}
		return "";
		
		
	}
	
	public String toAssembly(HashMap<String,String> vars ) {
		
		System.out.println(vars.values());
		
		
		return "";
		
	}
	
	
}
