//Nome: Nilo Conrado Messias Alves Cangerana	-	Número USP: 9805362

//No trabalho, foi implementado 1 escritor e 12 leitores.
//O escritor gera um numero aleatorio entre -500 e 500 e escreve esse valor no src\arquivo.txt
//O leitor i (i varia de 1 a 12) escolhe uma linha aleatoria do src\arquivo.txt, le o valor presente nessa linha e executa uma soma: i + valor lido.
//Somente 10 leitores podem ler ao mesmo tempo.
//O escritor e leitores nao podem executar ao mesmo tempo.

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class WriterReader {
	//Definicao dos objetos
	static escritor e = new escritor(); //instancia de uma nova thread escritor
	static leitor l1 = new leitor(1); //instancia de uma nova thread leitor 1
	static leitor l2 = new leitor(2); //instancia de uma nova thread leitor 2
	static leitor l3 = new leitor(3); //instancia de uma nova thread leitor 3
	static leitor l4 = new leitor(4); //instancia de uma nova thread leitor 4
	static leitor l5 = new leitor(5); //instancia de uma nova thread leitor 5
	static leitor l6 = new leitor(6); //instancia de uma nova thread leitor 6
	static leitor l7 = new leitor(7); //instancia de uma nova thread leitor 7
	static leitor l8 = new leitor(8); //instancia de uma nova thread leitor 8
	static leitor l9 = new leitor(9); //instancia de uma nova thread leitor 9
	static leitor l10 = new leitor(10); //instancia de uma nova thread leitor 10
	static leitor l11 = new leitor(11); //instancia de uma nova thread leitor 11
	static leitor l12 = new leitor(12); //instancia de uma nova thread leitor 12
	static monitor mon = new monitor(); //instancia de um novo monitor
	static Random rn1 = new Random();
	static String path = "src\\arquivo.txt"; //path do arquivo que representa a regiao critica
	static int qtdValores = 0; //variavel que conta a quantidade de valores que estao escritos no arquivo
	static int readerCount=0; //variavel que conta a quantidade de leitores lendo o arquivo no momento
	static int db=1; //controla regiao critica entre escritor e leitores
	
	//Definicao do escritor
	static class escritor extends Thread //thread escritor
	{
		private int geraValor() //metodo que gera numero randomico que sera escrito no arquivo
		{
			int val;
			val =  -500 + rn1.nextInt(500 - (-500) + 1); //gera um valor aleatorio entre -500 e 500
			return val;
		}

		public void run() //metodo run: contem o codigo da thread escritor
		{
			int valor;
			while(true) {
				valor = geraValor(); //gera o valor que vai ser escrito no arquivo
				mon.subDB(); //acessa variavel DB para decrementar.
				
				try {
					mon.escreve(valor);  //chama metodo escreve do monitor para escrever o valor gerado no arquivo.txt
				} catch (IOException e1) {
					System.out.println("Erro: Arquivo não encontrado.");
					System.exit(0);
				} 
				mon.upDB(); //acessa variavel DB para incrementar.
				
				try {
					sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	//Definicao dos leitores
	static class leitor extends Thread //Thread leitor
	{
		private int numeroLeitor; //variavel que indica o numero do leitor
		
		//Construtor do leitor
		public leitor(int numero) {
			this.numeroLeitor=numero;
		}
		
		private void soma(int valor) //executa uma soma: numeroLeitor + valor
		{
			System.out.println("Valor lido pelo leitor "+numeroLeitor+": "+ valor+ "    |	 Soma: "+numeroLeitor+" + ("+valor+") = " + (numeroLeitor+valor));
		}

		public void run() //metodo run: contem o codigo da thread leitor
		{
			int valor;
			String linha = ""; 
			
			while(true) {
				mon.addLeitor(); //acessa regiao critica da variavel readerCount para adicionar 1 leitor
				if(readerCount==1) { //se for o primeiro leitor, impede escritor decrementando variavel DB
					mon.subDB();
				}
				int valAleatorio;
				valAleatorio = 1 + rn1.nextInt(qtdValores - 1 + 1); // escolhe uma linha aleatoria do arquivo para ler
			
				try {
					BufferedReader buffRead = new BufferedReader(new FileReader(path));
					for(int i=1;i<=valAleatorio;i++) //procura a linha no arquivo
					{
						linha = buffRead.readLine();
					}
					buffRead.close();
				} catch (IOException e) {
					e.printStackTrace();
					}
			
				valor=Integer.parseInt(linha); //atribui o valor lido da linha
				mon.subLeitor(); //leitor para de ler, decrementa variavel readerCount
			
				if(readerCount==0) { //se a quantidade de leitores e zero, escritor pode escrever, incrementando variavel DB
					mon.upDB();
				}
			
				soma(valor); //executa operacao soma do leitor com valor lido
			
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
			}
		}
	}
	
	//Definicao do Monitor
	static class monitor
	{
		private void go_to_sleep() //funcao que poe a thread para dormir
		{
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	
	
		//synchronized garante que somente uma tread tera acesso a regiao critica por vez
		public synchronized void escreve(int val) throws IOException //metodo que escreve no arquivo
		{
			BufferedWriter buffWrite = new BufferedWriter(new FileWriter(path,true));
			System.out.println("Valor escrito: "+val);
			qtdValores++; //incrementa quantidade
			buffWrite.append(""+ val + "\n"); //escreve valor no arquivo
			buffWrite.close();
			
			if(qtdValores == 1) //se leitor estava dormindo por falta de valores no arquivo, ele e acordado
			{
				notify();
			}
		}
		
		//Adiciona 1 a quantidade de leitores lendo o arquivo.txt
		public synchronized void addLeitor() {
			if(qtdValores==0)//se nao existe nada para ler, o leitor e colocado para dormir
			{
				go_to_sleep();
			}
			if(readerCount==10) { //se a quantidade de leitores for 10, o proximo leitor e colocado para dormir
				go_to_sleep();
			}
			readerCount++;
		}
		//Subtrai em 1 a quantidade de leitores lendo o arquivo.txt
		public synchronized void subLeitor() {
			if(readerCount==0) {
				go_to_sleep();
			}
			readerCount--;

		}
		
		//Subtrai em 1 a variavel db
		public synchronized void subDB() {
			if(db==0) { //se db for 0, dorme
				go_to_sleep();
			}
			db--;

		}
		//Adiciona em 1 a variavel db
		public synchronized void upDB() {
			if(db==1) {
				go_to_sleep();
			}
			db++;

		}
	}
	
	//Funcao Main
	public static void main(String args[]) throws IOException
	{
		BufferedWriter buffWrite = new BufferedWriter(new FileWriter(path)); //Apaga conteudo do arquivo.txt antes de iniciar a execucao
		buffWrite.append("");
		buffWrite.close();
		
		e.start(); //inicia o thread escritor
		l1.start(); //inicia o thread leitor 1
		l2.start(); //inicia o thread leitor 2
		l3.start(); //inicia o thread leitor 3
		l4.start(); //inicia o thread leitor 4
		l5.start(); //inicia o thread leitor 5
		l6.start(); //inicia o thread leitor 6
		l7.start(); //inicia o thread leitor 7
		l8.start(); //inicia o thread leitor 8
		l9.start(); //inicia o thread leitor 9
		l10.start(); //inicia o thread leitor 10
		l11.start(); //inicia o thread leitor 11
		l12.start(); //inicia o thread leitor 12
	}
	
}
