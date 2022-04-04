package fr.usmb.m1isc.compilation.tp;

import java.util.HashMap;
import java.util.List;

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
	
	public String toAssembly(List<String> vars) {
		String text = "DATA SEGMENT\n";
		
		for (String elem : vars) {
			text+="\t" + elem + "DD\n";
		}
		text += "DATA ENDS\n" + "CODE SEGMENT\n";
		
        
        text += "CODE ENDS";
        return text;
    }
	
	private String convertArbre(Arbre arbre, boolean sens) {
		String text = "";
		switch (arbre.racine) {
		case ";":
			text = sens==true?
					convertArbre(arbre.getArbreG(), !sens) + "\n" + convertArbre(arbre.getArbreD(), !sens):
					convertArbre(arbre.getArbreD(), !sens) + "\n" + convertArbre(arbre.getArbreG(), !sens);
			break;
			
		case "LET":
			text = "mov eax, " + arbre.getArbreD() + "\n"
					+ "mov " + arbre.getArbreG() + ", eax\n"
					+ "mov eax, " + arbre.getArbreG() + "\n"
					+ "push eax";
			break;
			
		case "*":
			text = "mov eax, " + arbre.getArbreD() + "\n"
					+ "mov " + arbre.getArbreG() + ", eax\n"
					+ "mov eax, " + arbre.getArbreG() + "\n"
					+ "push eax";
			break;

		default:
			text = "Error";
			break;
		}
		return text;
	}
}
