package fr.usmb.m1isc.compilation.tp;

import java.util.ArrayList;
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
		vars = removeDoublon(vars);
		for (String elem : vars) {
			text+="\t" + elem + " DD\n";
		}
		text += "DATA ENDS\n" + "CODE SEGMENT\n";
		text+=convertArbre(this,true, vars,false);
		//convertArbre(this,true, vars);
        
        text += "CODE ENDS";
        return text;
    }
	
	private List<String> removeDoublon(List<String> vars) {
		List<String> l = new ArrayList<String>();
		boolean existe;
		for (String s : vars) {
			existe=false;
			for (String s2 : l) {
				if(s2.equals(s)) {
					existe=true;
					break;
				}
			}
			if(!existe) {
				l.add(s);
			}
		}
		return l;
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
				if(!lastOp && !arbre.getArbreD().racine.equals("INPUT")) {
					text += "\t mov eax, "+ arbre.getArbreG().racine + "\n";
					text += "\t push eax\n";
				}
				
				break;
				
			case "*":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
					text+=convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
					text+="\t pop ebx\n";
					text+="\t mul ebx, eax\n";
					text+="\t mov eax, ebx\n";
					text+="\t push eax\n";
				}
				else {
					text+=convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
					text+=convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
					text+="\t mul eax, ebx\n";
					text+="\t push eax\n";
				}
				break;
			case "/":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
					text+=convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
					text+="\t pop ebx\n";
					text+="\t div ebx, eax\n";
					text+="\t mov eax, ebx\n";
					
					text+="\t push eax\n";
				}
				else {
					text+=convertArbre(arbre.getArbreD(), !sens,vars,lastOp);
					text+=convertArbre(arbre.getArbreG(), !sens,vars,lastOp);
					text+="\t div eax, ebx\n";
					text+="\t push eax\n";
				}
				break;
			case "WHILE":
				text+="debut_while_1:\n";
				text+= convertArbreCond(arbre.getArbreG(),vars);
				text+="faux_gt_1:\n";
				text+="\t mov eax,0\n";
				text+="sortie_gt_1:\n";
				text+="\t jz sortie_while_1\n";
				text+= convertArbreAction(arbre.getArbreD(),vars);
				text+="\t jmp debut_while_1\n";
				text+="sortie_while_1:\n";
				break;
				
			case "INPUT":
				text+="\t in eax\n";
				break;
			case "OUTPUT":
				text+="\t mov eax,"+ arbre.getArbreG().racine+"\n";
				text+="\t out eax\n";
				break;
	
			default:
				if(isNumeric(arbre.racine))
				{
					text+="\t mov eax, " + arbre.racine +"\n";
				}
				else if(isVar(vars,arbre.racine))
				{
					text+="\t mov "+arbre.racine+", eax\n";
				}
				else {
					text += "Error";
				}
				break;
		}
		return text;
	}

	private String convertArbreAction(Arbre arbre,List<String> vars) {
		String text = "";
		
		switch (arbre.racine) {
			case ";":
				text += convertArbreAction(arbre.getArbreG(),vars);
				text += convertArbreAction(arbre.getArbreD(),vars);
						
				break;
			case "LET":
				text += convertArbreAction(arbre.getArbreD(),vars);
				text += "\t mov "+arbre.getArbreG().racine+", eax\n";

				break;
			case "%":
				text += convertArbreAction(arbre.getArbreD(),vars);
				text += "\t push eax\n";
				text += convertArbreAction(arbre.getArbreG(),vars);
				text += "\t pop ebx\n";
				text += "\t mov ecx,eax\n";
				text += "\t div ecx,ebx\n";
				text += "\t mul ecx,ebx\n";
				text += "\t sub eax,ecx\n";
				break;
				
				
			case "*":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreAction(arbre.getArbreG(),vars);
					text+=convertArbreAction(arbre.getArbreD(),vars);
					text+="\t pop ebx\n";
					text+="\t mul ebx, eax\n";
					text+="\t mov eax, ebx\n";
				}
				else {
					text+=convertArbreAction(arbre.getArbreD(),vars);
					text+=convertArbreAction(arbre.getArbreG(),vars);
					text+="\t mul eax, ebx\n";
				}

				break;
			case "/":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreAction(arbre.getArbreG(),vars);
					text+=convertArbreAction(arbre.getArbreD(),vars);
					text+="\t pop ebx\n";
					text+="\t div ebx, eax\n";
					text+="\t mov eax, ebx\n";
				}
				else {
					text+=convertArbreAction(arbre.getArbreD(),vars);
					text+=convertArbreAction(arbre.getArbreG(),vars);
					text+="\t div eax, ebx\n";
					
				}
				break;
			case "":
				text+=convertArbreAction(arbre.getArbreG(),vars);
				break;
			default:
				if(isNumeric(arbre.racine))
				{
					text+="\t mov eax, " + arbre.racine +"\n";
				}
				else if(isVar(vars,arbre.racine))
				{
					text+="\t mov eax, "+ arbre.racine+"\n";
				}
				else {
					text += "Error";
				}
					
				break;
		}
		return text;
	}
	private String convertArbreCond(Arbre arbre, List<String> vars) {
		// TODO Auto-generated method stub
		String text = "";
		
		switch (arbre.racine) {
			
			case "<":
				text+=convertArbreCond(arbre.getArbreG(),vars);
				text+="\t push eax\n";
				text+=convertArbreCond(arbre.getArbreD(),vars);
				text+="\t pop ebx\n";
				text+="\t sub eax,ebx\n";
				text+="\t jle faux_gt_1\n";
				text+="\t mov eax,1\n";
				text+="\t jmp sortie_gt_1\n";
				
				break;
			case "":
				text+=convertArbreCond(arbre.getArbreG(),vars);
				break;
			default:
				if(isNumeric(arbre.racine))
				{
					text+="\t mov eax, " + arbre.racine +"\n";
				}
				else if(isVar(vars,arbre.racine))
				{
					text+="\t mov eax, "+ arbre.racine +"\n";
				}
				else {
					System.out.println("\nYOOOO" + arbre.racine + "\n");
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
					text += "\t mov eax, "+ arbre.getArbreG().racine + "\n";
					text += "\t push eax\n";
				}
				break;
				
			case "*":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp);
					text+="\t pop ebx\n";
					text+="\t mul ebx, eax\n";
					text+="\t mov eax, ebx\n";
				}
				else {
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
					text+="\t mul eax, ebx\n";
				}
				
				if(profondeur!=1 || !lastOp) {
					text+="\t push eax\n";
				}
				break;
			case "/":
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp);
					text+="\t pop ebx\n";
					text+="\t div ebx, eax\n";
					text+="\t mov eax, ebx\n";
				}
				else {
					text+=convertArbreLetPartieDroite(arbre.getArbreD(), !sens,vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreG(), !sens,vars,profondeur+1,lastOp);
					text+="\t div eax, ebx\n";
					
				}
				if(profondeur!=1 || !lastOp) {
					text+="\t push eax\n";
				}
				break;
			case "INPUT":
				text+="\t in eax\n";
				break;
			default:
				if(isNumeric(arbre.racine))
				{
					text+="\t mov eax, " + arbre.racine +"\n";
				}
				else if(isVar(vars,arbre.racine))
				{
					text+="\t pop ebx\n";
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
