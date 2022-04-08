package fr.usmb.m1isc.compilation.tp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class Arbre {
	//Variable globale
	public static String stack = "ebx";
    //Enmération
	public static enum Type {SEMI,LET,WHILE,INPUT,OUTPUT,INF,PLUS,MOINS,DIV,MUL,MOD,ENTIER,VIDE,EQ,IF,THEN,NOT,AND,OR,INFEQ,MINUS,NULL,IDENT};
	//Opération possible
	public String[] op ={"/","*","+","-"};
	
    //Attributs
	private Type type;
	private String racine;
	private Arbre arbreG;
	private Arbre arbreD;
	
    //Getter et Setters
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
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
	
    //Constructeur
	public Arbre(String racine, Arbre arbreG, Arbre arbreD,Type type) {
		super();
		this.racine = racine;
		this.arbreG = arbreG;
		this.arbreD = arbreD;
		this.type=type;
	}
	
    //On réécrit toString pour afficher le type Arbre
    // cela dépend du contenu des fils et de la racine
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
	
    //Permet de convertir une "séquence" (dans notre cup) vers un language en Assembly
    //On se servs d'une HashMap "vars" pour connaitre le nom des variables
	public String toAssembly(List<String> vars) {
        
        //On définis les variables
		String text = "DATA SEGMENT\n";
		vars = removeDoublon(vars);        
		for (String elem : vars) {
			text+="\t" + elem + " DD\n";
		}
		text += "DATA ENDS\n" + "CODE SEGMENT\n";

        //On convertie le code
		text+=convertArbre(this, vars, false);
        
        text += "CODE ENDS";
        return text;
    }
	
    //Permet d'enlever les doublons dans les variables
	private List<String> removeDoublon(List<String> vars) {
        //Création d'un tableau vide
		List<String> l = new ArrayList<String>();
		boolean existe;
        //On parcours les variables de vars
		for (String s : vars) {
			existe=false;
            //On parcours les variables de l pour vérifier que la variable courante n'est pas déjà présente
			for (String s2 : l) {
				if(s2.equals(s)) {
					existe=true;
					break;
				}
			}
            //Et on l'ajoute a l si elle n'est pas présente
			if(!existe) {
				l.add(s);
			}
		}
		return l;
	}

    //Permet de convertir un arbre en code Assembly
    //Cette fonction va se rappeler récursivement
    //Elle a besoin d'un arbre, d'une HashMap, et d'une variable pour savoir si il y a une opération qui a eu lieu
	private String convertArbre(Arbre arbre, List<String> vars, boolean lastOp) {
		String text = "";
		
        //On trouve le type de l'abre
		switch (arbre.type) {

            //Si c'est un ";"
			case SEMI:
				text += convertArbre(arbre.getArbreG(),vars,lastOp);
				if(arbre.getArbreD().racine == ";")
					text += convertArbre(arbre.getArbreD(),vars,lastOp);
				else {
					text += convertArbre(arbre.getArbreD(),vars,true);
				}	
				break;
				
            //Si c'est un "let"
			case LET:
				text += convertArbreLetPartieDroite(arbre.getArbreD(),vars,1,lastOp) + convertArbre(arbre.getArbreG(),vars,lastOp);
				if(!lastOp && !arbre.getArbreD().racine.equals("INPUT")) {
					text += "\tmov eax, "+ arbre.getArbreG().racine + "\n";
					text += "\tpush eax\n";
				}
				break;
				
            //Si c'est une "multiplication"
			case MUL:
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbre(arbre.getArbreG(),vars,lastOp);
					text+=convertArbre(arbre.getArbreD(),vars,lastOp);
					text+="\tpop ebx\n";
					text+="\tmul ebx, eax\n";
					text+="\tmov eax, ebx\n";
					text+="\tpush eax\n";
				}
				else {
					text+=convertArbre(arbre.getArbreD(),vars,lastOp);
					text+=convertArbre(arbre.getArbreG(),vars,lastOp);
					text+="\tmul eax, ebx\n";
					text+="\tpush eax\n";
				}
				break;

            //Si c'est une division
			case DIV:
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbre(arbre.getArbreG(),vars,lastOp);
					text+=convertArbre(arbre.getArbreD(),vars,lastOp);
					text+="\tpop ebx\n";
					text+="\tdiv ebx, eax\n";
					text+="\tmov eax, ebx\n";
					
					text+="\tpush eax\n";
				}
				else {
					text+=convertArbre(arbre.getArbreD(),vars,lastOp);
					text+=convertArbre(arbre.getArbreG(),vars,lastOp);
					text+="\tdiv eax, ebx\n";
					text+="\tpush eax\n";
				}
				break;

            //Si c'est un "while"
			case WHILE:
				text+="debut_while_1:\n";
				text+= convertArbreCond(arbre.getArbreG(),vars);
				text+="faux_gt_1:\n";
				text+="\tmov eax,0\n";
				text+="sortie_gt_1:\n";
				text+="\tjz sortie_while_1\n";
				text+= convertArbreAction(arbre.getArbreD(),vars);
				text+="\tjmp debut_while_1\n";
				text+="sortie_while_1:\n";
				break;
				
            //Si c'est une "input"
			case INPUT:
				text+="\tin eax\n";
				break;

            //Si c'est une "output"
			case OUTPUT:
				text+="\tmov eax,"+ arbre.getArbreG().racine+"\n";
				text+="\tout eax\n";
				break;
            
            //Si c'est un entier
			case ENTIER:
				text+="\tmov eax, " + arbre.racine +"\n";
				break;
            
            //Si c'est une variable
			case IDENT:
				text+="\tmov "+arbre.racine+", eax\n";
				break;
	
            //Sinon c'est une erreur
			default:
				text += "Error";
				break;
		}
		return text;
	}

    //Fonction récursive qui est utilisé pour le "while"
	private String convertArbreAction(Arbre arbre,List<String> vars) {
		String text = "";
		
		switch (arbre.type) {
            //Basique conversion a gauche et a droite pour faire tout l'arbre avec les ;
			case SEMI:
				text += convertArbreAction(arbre.getArbreG(),vars);
				text += convertArbreAction(arbre.getArbreD(),vars);
				break;

            //Gere les let avec des variables globables donc pas de probleme de pop de stack
			case LET:
				text += convertArbreAction(arbre.getArbreD(),vars);
				text += "\tmov "+arbre.getArbreG().racine+", eax\n";
				break;

            //Modulo
			case MOD:
				text += convertArbreAction(arbre.getArbreD(),vars);
				text += "\tpush eax\n";
				text += convertArbreAction(arbre.getArbreG(),vars);
				text += "\tpop ebx\n";
				text += "\tmov ecx,eax\n";
				text += "\tdiv ecx,ebx\n";
				text += "\tmul ecx,ebx\n";
				text += "\tsub eax,ecx\n";
				break;
				
			//Cas parentheses
			case VIDE:
				text+=convertArbreAction(arbre.getArbreG(),vars);
				break;

			//Copie entier dans eax
			case ENTIER:
				text+="\tmov eax, " + arbre.racine +"\n";
				break;
			//Copie var globable dans eax
			case IDENT:
				text+="\tmov eax, "+ arbre.racine+"\n";
				break;
			default:
				text += "Error";
				break;
		}
		return text;
	}
    //Gere la partie condition de l'arbre
	private String convertArbreCond(Arbre arbre, List<String> vars) {
		String text = "";
		
		switch (arbre.type) {
			//Gere le inf 
			case INF:
				text+=convertArbreCond(arbre.getArbreG(),vars);
				text+="\tpush eax\n";
				text+=convertArbreCond(arbre.getArbreD(),vars);
				text+="\tpop ebx\n";
				text+="\tsub eax,ebx\n";
				text+="\tjle faux_gt_1\n";
				text+="\tmov eax,1\n";
				text+="\tjmp sortie_gt_1\n";
				
				break;
            //Ignore la valeur de la parenthese
			case VIDE:
				text+=convertArbreCond(arbre.getArbreG(),vars);
				break;
            //Copie entier dans eax
			case ENTIER:
				text+="\tmov eax, " + arbre.racine +"\n";
				break;
            //Copie var globable dans eax
			case IDENT:
				text+="\tmov eax, "+ arbre.racine +"\n";
				break;
			default:
				text += "Error";
					
				break;
		}
		return text;
		
		
	}
	//PARTIE DROITE
    //Gere la partie droite des let qui possede potentiellement des variables et donc faut pop la stack pour les recuperer
    //C'est quasiment la meme fonction que convertArbre mais elle a un parametre en plus permettant de savoir si on doit faire certain push en fonction si on est a la fin de la methode et a la bonne profondeur
	private String convertArbreLetPartieDroite(Arbre arbre,List<String> vars,int profondeur,boolean lastOp) {
		String text = "";
		
		switch (arbre.type) {
            //
			case SEMI:
				text += convertArbreLetPartieDroite(arbre.getArbreG(),vars,profondeur+1,lastOp);
				if(arbre.getArbreD().racine==";")
					text += convertArbreLetPartieDroite(arbre.getArbreD(),vars,profondeur+1, lastOp);
				else {
					text += convertArbreLetPartieDroite(arbre.getArbreD(),vars,profondeur+1,true);
				}
						
				break;
			
            //
			case LET:
				text += convertArbreLetPartieDroite(arbre.getArbreD(),vars,profondeur+1,lastOp) + convertArbreLetPartieDroite(arbre.getArbreG(),vars,profondeur+1,lastOp);
				if(!lastOp) {
					text += "\tmov eax, "+ arbre.getArbreG().racine + "\n";
					text += "\tpush eax\n";
				}
				break;
			//En fonction de si le prochain arbre a une operation cela changera le sens de l'operation et mov/pop ou non
			case MUL:
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreLetPartieDroite(arbre.getArbreG(),vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreD(),vars,profondeur+1,lastOp);
					text+="\tpop ebx\n";
					text+="\tmul ebx, eax\n";
					text+="\tmov eax, ebx\n";
				}
				else {
					text+=convertArbreLetPartieDroite(arbre.getArbreD(),vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreG(),vars,profondeur+1,lastOp);
					text+="\tmul eax, ebx\n";
				}
				
				if(profondeur!=1 || !lastOp) {
					text+="\tpush eax\n";
				}
				break;
			case DIV:
				if(Arrays.stream(op).anyMatch(arbre.getArbreG().racine::equals)) {
					text+=convertArbreLetPartieDroite(arbre.getArbreG(),vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreD(),vars,profondeur+1,lastOp);
					text+="\tpop ebx\n";
					text+="\tdiv ebx, eax\n";
					text+="\tmov eax, ebx\n";
				}
				else {
					text+=convertArbreLetPartieDroite(arbre.getArbreD(),vars,profondeur+1,lastOp);
					text+=convertArbreLetPartieDroite(arbre.getArbreG(),vars,profondeur+1,lastOp);
					text+="\tdiv eax, ebx\n";
					
				}
				if(profondeur!=1 || !lastOp) {
					text+="\tpush eax\n";
				}
				break;
            //Simple gestion de l'input
			case INPUT:
				text+="\tin eax\n";
				break;
			//Copie la valeur de l'entier dans eax
			case ENTIER:
				text+="\tmov eax, " + arbre.racine +"\n";
				break;
			//Vu qu'on est a droite du let alors on pop de la stack la variable
			case IDENT:
				text+="\tpop ebx\n";
				break;
			default:
				text += "Error";
				break;
		}
		return text;
	}
	
    //Permet de créer le fichier pgcd.asm,
    // et de lui mettre le code Assembly
	static public void makeFile(String str) {
		try {
		      File myObj = new File("pgcd.asm");
		      if (myObj.createNewFile()) {
		        System.out.println("Fichier créer: " + myObj.getName());
		      } else {
		        System.out.println("Le fichier existe déjà");
		      }
		    } catch (IOException e) {
		      System.out.println("Il y a une erreur dans la création du fichier");
		      e.printStackTrace();
		    }
		try {
	      FileWriter myWriter = new FileWriter("pgcd.asm");
	      myWriter.write(str);
	      myWriter.close();
	      System.out.println("Ecriture dans le fichier réussi");
	    } catch (IOException e) {
	      System.out.println("Il y a une erreur dans l'écriture du fichier");
	      e.printStackTrace();
	    }
	}
	
}
