package fr.usmb.m1isc.compilation.tp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class Arbre {
	//var globale
	public static String stack = "ebx";
	public String[] op ={"/","*","+","-"};
	
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
		text+=convertArbre(this,true, vars);
		//convertArbre(this,true, vars);
        
        text += "CODE ENDS";
        return text;
    }
	
	private String convertArbre(Arbre arbre, boolean sens,List<String> vars) {
		String text = "";
		
		switch (arbre.racine) {
			case ";":
				text += convertArbre(arbre.getArbreG(), !sens,vars) + "\n" + convertArbre(arbre.getArbreD(), !sens,vars);
						
				break;
				
			case "LET":
				text += convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars) + convertArbre(arbre.getArbreG(), !sens,vars);
				text += "mov eax, "+ arbre.getArbreG().racine + "\n";
				text += "push eax\n";
				break;
				
			case "*":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbre(arbre.getArbreG(), !sens,vars);
					text+=convertArbre(arbre.getArbreD(), !sens,vars);
					text+="pop ebx\n";
					text+="mul ebx, eax\n";
					text+="mov eax, ebx\n";
					text+="push eax\n";
				}
				else {
					text+=convertArbre(arbre.getArbreD(), !sens,vars);
					text+=convertArbre(arbre.getArbreG(), !sens,vars);
					text+="mul eax, ebx\n";
					text+="push eax\n";
				}
				break;
			case "/":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbre(arbre.getArbreG(), !sens,vars);
					text+=convertArbre(arbre.getArbreD(), !sens,vars);
					text+="pop ebx\n";
					text+="div ebx, eax\n";
					text+="mov eax, ebx\n";
					text+="push eax\n";
				}
				else {
					text+=convertArbre(arbre.getArbreD(), !sens,vars);
					text+=convertArbre(arbre.getArbreG(), !sens,vars);
					text+="div eax, ebx\n";
					text+="push eax\n";
				}
				break;
	
			default:
				if(isNumeric(arbre.racine))
				{
					text+="mov eax, " + arbre.racine +"\n";
				}
				else if(isVar(vars,arbre.racine))
				{
					text+="mov "+arbre.racine+", eax\n";
				}
				else {
					text += "Error";
				}
				break;
		}
		return text;
	}
	
	//PARTIE DROITE
	private String convertArbreLetPartieDroite(Arbre arbre, boolean sens,List<String> vars) {
		String text = "";
		
		switch (arbre.racine) {
			case ";":
				text += sens==true?
						convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars) + "\n" + convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars):
							convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars) + "\n" + convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars);
				break;
				
			case "LET":
				text += convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars) + convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars);
				
				text += "mov eax, "+ arbre.getArbreG().racine + "\n";
				text += "push eax\n";
				break;
				
			case "*":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars);
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars);
					text+="pop ebx\n";
					text+="mul ebx, eax\n";
					text+="mov eax, ebx\n";
					text+="push eax\n";
				}
				else {
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars);
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars);
					text+="mul eax, ebx\n";
					text+="push eax\n";
				}
				break;
			case "/":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars);
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars);
					text+="pop ebx\n";
					text+="div ebx, eax\n";
					text+="mov eax, ebx\n";
					text+="push eax\n";
				}
				else {
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars);
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars);
					text+="div eax, ebx\n";
					text+="push eax\n";
				}
				break;
	
			default:
				if(isNumeric(arbre.racine))
				{
					text+="mov eax, " + arbre.racine +"\n";
				}
				else if(isVar(vars,arbre.racine))
				{
					text+="pop ebx\n";
				}
				else {
					text += "Error";
				}
					
				break;
		}
		return text;
	}
	private boolean isVar(List<String> vars,String value) {
		for (String string : vars) {
			if(string.equals(value)) {
				return true;
			}
		}
		return false;
	}
	public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
