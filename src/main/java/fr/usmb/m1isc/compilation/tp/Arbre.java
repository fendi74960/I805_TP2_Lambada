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
		text+=convertArbre(this,true, vars,false);
		//convertArbre(this,true, vars);
        
        text += "CODE ENDS";
        return text;
    }
	
	private String convertArbre(Arbre arbre, boolean sens,List<String> vars,boolean lastOp) {
		String text = "";
		
		switch (arbre.racine) {
			case ";":
				text += convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
				if(arbre.getArbreD().racine==";")
					text += convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
				else {
					text += convertArbre(arbre.getArbreD(), !sens,vars,true);
				}
						
				break;
				
			case "LET":
				text += convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,1,lastOp) + convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
				if(!lastOp) {
					System.out.println("NO1\n");
					text += "mov eax, "+ arbre.getArbreG().racine + "\n";
					text += "push eax\n";
				}
				
				break;
				
			case "*":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
					text+=convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
					text+="pop ebx\n";
					text+="mul ebx, eax\n";
					text+="mov eax, ebx\n";
					text+="push eax\n";
				}
				else {
					text+=convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
					text+=convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
					text+="mul eax, ebx\n";
					text+="push eax\n";
				}
				break;
			case "/":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
					text+=convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
					text+="pop ebx\n";
					text+="div ebx, eax\n";
					text+="mov eax, ebx\n";
					
					text+="push eax\n";
				}
				else {
					text+=convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
					text+=convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
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
	private String convertArbreLetPartieDroite(Arbre arbre, boolean sens,List<String> vars,int profondeur,boolean lastOp) {
		String text = "";
		
		switch (arbre.racine) {
			case ";":
				text += convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
				if(arbre.getArbreD().racine==";")
					text += convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1, lastOp);
				else {
					text += convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,true);
				}
						
				break;
				
			case "LET":
				text += convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp) + convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
				if(!lastOp) {
					System.out.println("NO\n");
					text += "mov eax, "+ arbre.getArbreG().racine + "\n";
					text += "push eax\n";
				}
				break;
				
			case "*":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp);
					text+="pop ebx\n";
					text+="mul ebx, eax\n";
					text+="mov eax, ebx\n";
				}
				else {
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
					text+="mul eax, ebx\n";
				}
				
				if(profondeur!=1 || !lastOp) {
					text+="push eax\n";
				}
				break;
			case "/":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp);
					text+="pop ebx\n";
					text+="div ebx, eax\n";
					text+="mov eax, ebx\n";
				}
				else {
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
					text+="div eax, ebx\n";
					
				}
				if(profondeur!=1 || !lastOp) {
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
