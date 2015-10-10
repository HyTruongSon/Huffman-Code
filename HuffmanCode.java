// Software: Huffman code file compression
// Language: Java
// Author: Hy Truong Son
// Major: BSc. Computer Science
// Class: 2013 - 2016
// Institution: Eotvos Lorand University
// Email: sonpascal93@gmail.com
// Website: http://people.inf.elte.hu/hytruongson/
// Copyright 2015 (c) Hy Truong Son. All rights reserved.

import java.io.*;

public class HuffmanCode {

	public static int ByteValue = 256;
	public static int nBits = 8;
	
	public static int MaxHuffman = ByteValue * nBits;
	public static int MaxHeap = ByteValue + 1;
	
	public static class HuffmanNode {
		public int LeftChild, RightChild;
		public int isSymbol;
		public int Frequency;
	}
	
	public static int nHuffman, nSymbols;
	public static HuffmanNode Huffman[] = new HuffmanNode [MaxHuffman];
	
	public static int nHeap;
	public static int Heap[] = new int [MaxHeap]; 
	
	public static int Frequency[] = new int [ByteValue];
	
	public static int nLastBits;
	public static int Code[][];
	public static int Bit[] = new int [ByteValue];
	
	public static String GetType(String FileName){
		int i, j;
		String res;
		j = -1;
		for (i = 0; i < FileName.length(); i++)
			if (FileName.charAt(i) == '.'){
				j = i;
				break;
			}
		res = "";
		if (j == -1) return res;
		for (i = j + 1; i < FileName.length(); i++) res += FileName.charAt(i);
		return res;
	}
	
	public static void SwapHeap(int u, int v){
		int temp = Heap[u];
		Heap[u] = Heap[v];
		Heap[v] = temp;
	}
	
	public static boolean CompareHeap(int u, int v){
		if (Huffman[Heap[u]].Frequency < Huffman[Heap[v]].Frequency)
			return true;
		return false;
	}
	
	public static void UpHeap(int node){
		while (node > 1)
			if (CompareHeap(node, node / 2)){
				SwapHeap(node, node / 2);
				node /= 2;
			}else break;
	}
	
	public static void DownHeap(int node){
		int child;
		while (2 * node <= nHeap){
			child = 2 * node;
			if (child < nHeap)
				if (CompareHeap(child + 1, child)) child++;
			if (CompareHeap(child, node)){
				SwapHeap(child, node);
				node = child;
			}else break;
		}
	}
	
	public static void PushHeap(int v){
		nHeap++;
		Heap[nHeap] = v;
		UpHeap(nHeap);
	}
	
	public static int PopHeap(int node){
		int res = Heap[node];
		Heap[node] = Heap[nHeap];
		nHeap--;
		UpHeap(node);
		DownHeap(node);
		return res;
	}
	
	public static boolean Analysis(String FileName) throws IOException {
		BufferedInputStream file = new BufferedInputStream (new FileInputStream(FileName));
		
		if (file.available() == 0){
			file.close();
			return false;
		}
		
		for (int i = 0; i < 256; i++) Frequency[i] = 0;
		int aByte;
		
		while (file.available() > 0){
			aByte = file.read();
			Frequency[aByte]++;
		}
		
		file.close();
		return true;
	}
	
	public static void Visit(int node, int Length, int Bit[]){
		if (Huffman[node].isSymbol == -1){
			Bit[Length] = 0;
			Visit(Huffman[node].LeftChild, Length + 1, Bit);
			
			Bit[Length] = 1;
			Visit(Huffman[node].RightChild, Length + 1, Bit);
			
			return;
		}
		
		int i = Huffman[node].isSymbol;
		Code[i] = new int [Length];
		for (int j = 0; j < Length; j++)
			Code[i][j] = Bit[j];
	}
	
	public static void Init(){
		nHuffman = 0;
		for (int i = 0; i < ByteValue; i++)
			if (Frequency[i] > 0){
				Huffman[nHuffman] = new HuffmanNode();
				Huffman[nHuffman].LeftChild = 0;
				Huffman[nHuffman].RightChild = 0;
				Huffman[nHuffman].isSymbol = i;
				Huffman[nHuffman].Frequency = Frequency[i];
				nHuffman++;
			}
		
		nSymbols = nHuffman;
		Code = new int [ByteValue][];
		
		if (nSymbols == 1){
			int i = Huffman[nSymbols - 1].isSymbol;
			Code[i] = new int [1];
			Code[i][0] = 1;
			return;
		}
		
		nHeap = 0;
		for (int i = 0; i < nHuffman; i++)
			PushHeap(i);
		
		int LeftChild, RightChild;	
		while (nHeap > 1){
			RightChild = PopHeap(1);
			LeftChild = PopHeap(1);
			
			Huffman[nHuffman] = new HuffmanNode();
			Huffman[nHuffman].LeftChild = LeftChild;
			Huffman[nHuffman].RightChild = RightChild;
			Huffman[nHuffman].isSymbol = -1;
			Huffman[nHuffman].Frequency = Huffman[LeftChild].Frequency + Huffman[RightChild].Frequency;
			nHuffman++;
			
			PushHeap(nHuffman - 1);
		}
		
		Visit(nHuffman - 1, 0, Bit);
	}
	
	public static void EstimateLastByte(String FileName) throws IOException {
		BufferedInputStream file = new BufferedInputStream (new FileInputStream(FileName));
		
		int aByte;
		nLastBits = 0;
		while (file.available() > 0){
			aByte = file.read();
			nLastBits = (nLastBits + Code[aByte].length) % nBits;
		}
		
		file.close();
	}
	
	public static void WriteHeading(BufferedOutputStream Output) throws IOException {
		Output.write(nSymbols - 1);
		
		for (int v = 0; v < nSymbols; v++){
			int i = Huffman[v].isSymbol;
			Output.write(i);
			Output.write(Code[i].length);
			int sum = 0;
			for (int j = 0; j < Code[i].length; j++){
				sum += Code[i][j] * (1 << (j % nBits));
				if ((j + 1) % nBits == 0){
					Output.write(sum);
					sum /= ByteValue;
				}
			}
			if (Code[i].length % nBits != 0)
				Output.write(sum);
		}
		
		Output.write(nLastBits);
	}
	
	public static void Encrypt(String InputName, String OutputName) throws IOException {
		BufferedInputStream Input = new BufferedInputStream (new FileInputStream(InputName));
		BufferedOutputStream Output = new BufferedOutputStream(new FileOutputStream(OutputName));
		
		WriteHeading(Output);
		
		int aByte, top = 0, value = 0;
		while (Input.available() > 0){
			aByte = Input.read();
			for (int i = 0; i < Code[aByte].length; i++){
				value += Code[aByte][i] * (1 << top);
				top++;
				if (top == nBits){
					Output.write(value);
					top = 0;
					value = 0;
				}
			}
		}
		
		if (top > 0)
			Output.write(value);
			
		Input.close();
		Output.close();
	}
	
	public static void AddHuffman(int LeftChild, int RightChild, int isSymbol){
		Huffman[nHuffman] = new HuffmanNode();
		Huffman[nHuffman].LeftChild = LeftChild;
		Huffman[nHuffman].RightChild = RightChild;
		Huffman[nHuffman].isSymbol = isSymbol;
		nHuffman++;
	}
	
	public static void Decrypt(String InputName, String OutputName) throws IOException {
		int i, j, v, symbol, Length, aByte;
		
		BufferedInputStream Input = new BufferedInputStream (new FileInputStream(InputName));
		BufferedOutputStream Output = new BufferedOutputStream(new FileOutputStream(OutputName));
		
		nSymbols = Input.read() + 1;
		
		nHuffman = 0;
		AddHuffman(0, 0, -1);
		
		if (nSymbols == 1){
			AddHuffman(0, 0, Input.read());
			Huffman[0].RightChild = 1;
			
			Input.read();
			Input.read();
		}else
			for (v = 0; v < nSymbols; v++){
				symbol = Input.read();
				Length = Input.read();
				aByte = Input.read();
				
				i = 0;
				for (j = 0; j < Length; j++){
					if (aByte % 2 == 0){
						if (Huffman[i].LeftChild == 0){
							AddHuffman(0, 0, -1);
							Huffman[i].LeftChild = nHuffman - 1;
						}
						i = Huffman[i].LeftChild;
					}else{
						if (Huffman[i].RightChild == 0){
							AddHuffman(0, 0, -1);
							Huffman[i].RightChild = nHuffman - 1;
						}
						i = Huffman[i].RightChild;
					}
					aByte /= 2;
					
					if ((j + 1 < Length) && ((j + 1) % nBits == 0))
						aByte = Input.read();
				}
				
				Huffman[i].isSymbol = symbol;
			}
		
		nLastBits = Input.read();
		
		i = 0;
		while (Input.available() > 0){
			aByte = Input.read();
			
			v = nBits;
			if (nLastBits > 0)
				if (Input.available() == 0) v = nLastBits;
				
			for (j = 0; j < v; j++){
				if (aByte % 2 == 0)
					i = Huffman[i].LeftChild;
				else
					i = Huffman[i].RightChild;
				
				aByte /= 2;
					
				if (Huffman[i].isSymbol != -1){
					Output.write(Huffman[i].isSymbol);
					i = 0;
				}
			}
		}
			
		Input.close();
		Output.close();
	}

	public static void main(String args[]) throws IOException {	
		if (args.length < 2) return;
		
		if (args[0].equals("-e")){
			String InputName = args[1];
			String OutputName = args[2];
			
			if (Analysis(InputName)){
				Init();
				EstimateLastByte(InputName);
				Encrypt(InputName, OutputName);
			}else
				System.out.println("[" + InputName + " is an empty file]");
		}
		
		if (args[0].equals("-d")){
			String InputName = args[1];
			String OutputName = args[2];
			
			Decrypt(InputName, OutputName);
		}
	}	
	
}
