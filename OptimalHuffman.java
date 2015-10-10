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
import java.util.*;

public class OptimalHuffman {
	
	static int Byte = 1 << 8;
	static int Max_nSegments = 1 << 20;
	static int Max_nNodes = 8 * Byte;
	
	static int nSegments, nBuff, nBytes, nSymbols, nLastBits;
	static int Segment[] = new int [Max_nSegments];
	static int Buff[] = new int [Max_nSegments];
	
	static int Frequency[] = new int [Byte];
	
	static class AVL_Node {
		public int node;
		public int left, right, parent, balance, height;
	}
	static int nAVL, AVL_root;
	static AVL_Node AVL[] = new AVL_Node [Max_nNodes];
	
	static class Huffman_Node {
		public int symbol, prob;
		public int left, right;
	}
	static int nHuffman, Huffman_root;
	static Huffman_Node Huffman[] = new Huffman_Node [Max_nNodes];
	
	static int top, SumByte;
	static int BitStack[] = new int [Byte];
	
	static int Dictionary[][] = new int [Byte][];
	
	public static void AddNewNode(int symbol, int left, int right){
		Huffman[nHuffman] = new Huffman_Node();
		Huffman[nHuffman].symbol = symbol;
		Huffman[nHuffman].left = left;
		Huffman[nHuffman].right = right;
		nHuffman++;
	}
	
	public static void AddNewNode(int symbol, int left, int right, int prob){
		Huffman[nHuffman] = new Huffman_Node();
		Huffman[nHuffman].symbol = symbol;
		Huffman[nHuffman].left = left;
		Huffman[nHuffman].right = right;
		Huffman[nHuffman].prob = prob;
		nHuffman++;
	}
	
	public static void AddNewNode(int node, int left, int right, int parent, int balance, int height){
		AVL[nAVL] = new AVL_Node();
		AVL[nAVL].node = node;
		AVL[nAVL].left = left;
		AVL[nAVL].right = right;
		AVL[nAVL].parent = parent;
		AVL[nAVL].balance = balance;
		AVL[nAVL].height = height;
		nAVL++;
	}
	
	public static void ReCalculate(int u){
		int left = AVL[u].left;
		int right = AVL[u].right;
		
		int Left_Height = 0;
		int Right_Height = 0;
		
		if (left != -1) Left_Height = AVL[left].height;
		if (right != -1) Right_Height = AVL[right].height;
		
		AVL[u].height = Math.max(Left_Height, Right_Height) + 1;
		AVL[u].balance = Left_Height - Right_Height;
	}
	
	public static void link(int A, int B, int direction){
		if ((direction == 0) || (A == -1)){
			AVL_root = B;
			if (AVL_root != -1)
				AVL[AVL_root].parent = -1;
			return;
		}
		
		if (direction == 1)
			AVL[A].left = B;
		else
			AVL[A].right = B;
			
		if (B != -1)
			AVL[B].parent = A;
	}
	
	public static void Right_Rotation(int A){
		int B, C, direction;
		
		C = AVL[A].parent;
		direction = 0;
		if (C != -1)
			if (AVL[C].left == A)
				direction = 1;
			else
				direction = 2;
		
		B = AVL[A].left;	
		
		link(A, AVL[B].right, 1);
		link(B, A, 2);
		link(C, B, direction);
		
		ReCalculate(A);
		ReCalculate(B);
	}
	
	public static void Left_Rotation(int A){
		int B, C, direction;
		
		C = AVL[A].parent;
		direction = 0;
		if (C != -1)
			if (AVL[C].left == A)
				direction = 1;
			else
				direction = 2;
		
		B = AVL[A].right;
		
		link(A, AVL[B].left, 2);
		link(B, A, 1);
		link(C, B, direction);
		
		ReCalculate(A);
		ReCalculate(B);
	}
	
	public static void ReBalance(int u){
		int v, left, right;
		
		while (u != -1){
			v = AVL[u].parent;
			ReCalculate(u);
			
			if (AVL[u].balance == 2){
				left = AVL[u].left;
				if (AVL[left].balance >= 0)
					Right_Rotation(u);
				else{
					Left_Rotation(left);
					Right_Rotation(u);
				}
			}
			
			if (AVL[u].balance == -2){
				right = AVL[u].right;
				if (AVL[right].balance <= 0)
					Left_Rotation(u);
				else{
					Right_Rotation(right);
					Left_Rotation(u);
				}
			}
			
			u = v;
		}
	}
	
	public static int Compare(int u, int v){
		if (Huffman[u].prob < Huffman[v].prob) return -1;
		if (Huffman[u].prob > Huffman[v].prob) return 1;
		return 0;
	}
	
	public static void Insert(int node){
		if (AVL_root == -1){
			AddNewNode(node, -1, -1, -1, 0, 1);
			AVL_root = nAVL - 1;
			return;
		}
		
		int root = AVL_root;
		while (true)
			if (Compare(node, AVL[root].node) <= 0){
				if (AVL[root].left == -1){
					AddNewNode(node, -1, -1, root, 0, 1);
					AVL[root].left = nAVL - 1;
					ReBalance(nAVL - 1);
					break;
				}
				root = AVL[root].left;
			}else{
				if (AVL[root].right == -1){
					AddNewNode(node, -1, -1, root, 0, 1);
					AVL[root].right = nAVL - 1;
					ReBalance(nAVL - 1);
					break;
				}
				root = AVL[root].right;
			}
	}
	
	public static void Delete(int A){
		int B, C, D, direction;
		
		direction = 0;
		C = AVL[A].parent;
		if (C != -1)
			if (AVL[C].left == A) direction = 1; else
				direction = 2;
		
		B = AVL[A].left;
		
		if (B == -1){
			link(C, AVL[A].right, direction);
			ReBalance(C);
			return;
		}
		
		if (AVL[B].right == -1){
			link(B, AVL[A].right, 2);
			ReBalance(B);
			return;
		}
		
		while (AVL[B].right != -1)
			B = AVL[B].right;
		D = AVL[B].parent;
		
		link(D, AVL[B].left, 2);
		link(B, AVL[A].left, 1);
		link(B, AVL[A].right, 2);
		link(C, B, direction);
		
		ReBalance(D);
	}
	
	public static int PopMin(){
		int u = AVL_root;
		while (AVL[u].left != -1)
			u = AVL[u].left;
		Delete(u);
		return AVL[u].node;
	}
	
	public static void AVL_Visit(int u){
		if (AVL[u].left != -1)
			AVL_Visit(AVL[u].left);
		
		if (AVL[u].right != -1)
			AVL_Visit(AVL[u].right);
		
		System.out.println(AVL[u].node + " " + AVL[u].left + " " + AVL[u].right);
		u = AVL[u].node;
		System.out.println(Huffman[u].symbol + " " + Huffman[u].prob);
	}
	
	public static void Create_Dictionary(int node, int length, int bit[]){
		if (Huffman[node].left != -1){
			bit[length] = 0;
			Create_Dictionary(Huffman[node].left, length + 1, bit);
		}
		
		if (Huffman[node].right != -1){
			bit[length] = 1;
			Create_Dictionary(Huffman[node].right, length + 1, bit);
		}
		
		if (Huffman[node].symbol != -1){
			int i = Huffman[node].symbol;
			Dictionary[i] = new int [length];
			for (int j = 0; j < length; j++)
				Dictionary[i][j] = bit[j];
		}
	}
	
	public static void Init(){
		int i, u, v;
		for (i = 0; i < Byte; i++) Frequency[i] = 0;
		for (i = 0; i < nSegments; i++)
			Frequency[Segment[i]]++;
			
		nAVL = 0;
		AVL_root = -1;
		nHuffman = 0;
		nSymbols = 0;
		
		for (i = 0; i < Byte; i++)
			if (Frequency[i] > 0){
				nSymbols++;
				AddNewNode(i, -1, -1, Frequency[i]);
			}
		
		if (nSymbols == 1){
			int symbol = Huffman[0].symbol;
			Dictionary[symbol] = new int [1];
			Dictionary[symbol][0] = 1;
			return; 
		}	
		
		for (i = 0; i < nHuffman; i++) Insert(i);
		
		while (AVL_root != -1){
			u = PopMin();
			
			if (AVL_root == -1) break;
			
			v = PopMin();
			AddNewNode(-1, v, u, Huffman[u].prob + Huffman[v].prob);
			Insert(nHuffman - 1);
		}
		
		Huffman_root = nHuffman - 1;
		
		Create_Dictionary(Huffman_root, 0, BitStack);	
	}
	
	public static void Write_Heading(BufferedOutputStream Output) throws IOException {
		int i, j, v;
		
		Output.write(nSymbols % Byte);
		Output.write(nSymbols / Byte);
		
		for (v = 0; v < nSymbols; v++){
			i = Huffman[v].symbol;
			
			Output.write(i);
			Output.write(Dictionary[i].length);
			
			top = 0;
			SumByte = 0;
			for (j = 0; j < Dictionary[i].length; j++){
				SumByte += Dictionary[i][j] * (1 << top);
				top++;
				if (top == 8){
					Output.write(SumByte);
					top = 0;
					SumByte = 0;
				}
			}
			
			if (top > 0) Output.write(SumByte);
		}
		
		nBytes = 0;
		i = nBuff;
		while (i > 0){
			nBytes++;
			i /= Byte;
		}
		
		Output.write(nBytes);
		i = nBuff;
		while (i > 0){
			Output.write(i % Byte);
			i /= Byte;
		}
		
		Output.write(nLastBits);
	}
	
	public static void Encrypt(BufferedOutputStream Output) throws IOException {
		top = 0;
		SumByte = 0;
		nBuff = 0;
		
		int aByte;
		for (int i = 0; i < nSegments; i++){
			aByte = Segment[i];
			for (int j = 0; j < Dictionary[aByte].length; j++){
				SumByte += Dictionary[aByte][j] * (1 << top);
				top++;
				if (top == 8){
					Buff[nBuff] = SumByte;
					nBuff++;
					SumByte = 0;
					top = 0;
				}
			}
		}
		
		nLastBits = 8;
		if (top > 0){
			nLastBits = top;
			Buff[nBuff] = SumByte;
			nBuff++;
		}
		
		Write_Heading(Output);
		
		for (int i = 0; i < nBuff; i++)
			Output.write(Buff[i]);
	}
	
	public static void EncryptionNotice(){
		System.out.println("[Completed compression for one " + nSegments + "-byte segment]");
	}
	
	public static void Encrypt(String InputName, String OutputName) throws IOException {
		BufferedInputStream Input = new BufferedInputStream(new FileInputStream(InputName));
		BufferedOutputStream Output = new BufferedOutputStream(new FileOutputStream(OutputName));
		
		nSegments = 0;
		while (Input.available() > 0){
			Segment[nSegments] = Input.read();
			nSegments++;
			if (nSegments == Max_nSegments){
				Init();
				Encrypt(Output);
				EncryptionNotice();
				nSegments = 0;
			}
		}
		
		if (nSegments > 0){
			Init();
			Encrypt(Output);
			EncryptionNotice();	
		}
		
		Input.close();
		Output.close();
	}
	
	public static void Read_Heading(BufferedInputStream Input) throws IOException {
		int i, j, counting, root, symbol, length, aByte;
		
		nSymbols = Input.read() + Input.read() * Byte;
		
		nHuffman = 0;
		AddNewNode(-1, -1, -1);
		
		for (i = 0; i < nSymbols; i++){
			symbol = Input.read();
			length = Input.read();
			
			counting = 0;
			root = 0;
			
			while (counting < length){
				aByte = Input.read();
				
				for (j = 0; j < 8; j++){
					if (aByte % 2 == 0){
						if (Huffman[root].left == -1){
							AddNewNode(-1, -1, -1);
							Huffman[root].left = nHuffman - 1;
						}
						root = Huffman[root].left;
					}else{
						if (Huffman[root].right == -1){
							AddNewNode(-1, -1, -1);
							Huffman[root].right = nHuffman - 1;
						}
						root = Huffman[root].right;
					}
					aByte /= 2;
					
					counting++;
					if (counting == length) break;
				}
			}
			
			Huffman[root].symbol = symbol;
		}
		
		nBytes = Input.read();
		nBuff = 0;
		for (i = 0; i < nBytes; i++)
			nBuff += Input.read() * (1 << (8 * i));
		nLastBits = Input.read();
	}
	
	public static void DecryptionNotice(){
		System.out.println("[Completed decompression for one " + nSegments + "-byte segment]");
	}
	
	public static void Decrypt(String InputName, String OutputName) throws IOException {
		BufferedInputStream Input = new BufferedInputStream(new FileInputStream(InputName));
		BufferedOutputStream Output = new BufferedOutputStream(new FileOutputStream(OutputName));
		
		int i, j, aByte, root;
		
		root = 0;
		while (Input.available() > 0){
			Read_Heading(Input);
			
			nSegments = 0;
			for (i = 0; i < nBuff; i++){
				aByte = Input.read();
				
				for (j = 0; j < 8; j++){
					if (aByte % 2 == 0)
						root = Huffman[root].left;
					else
						root = Huffman[root].right;
					aByte /= 2;
					
					if (Huffman[root].symbol != -1){
						Output.write(Huffman[root].symbol);
						nSegments++;
						root = 0;
						
						if (i == nBuff - 1)
							if (j + 1 == nLastBits) break;
					}
				}
			}
			
			DecryptionNotice();
		}
		
		Input.close();
		Output.close();
	}
	
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
	
	public static void main(String args[]) throws IOException {
		if (args.length < 2) return;
		
		if ((!args[0].equals("-e")) && (!args[0].equals("-d"))) return;
		
		String InputName = args[1];
		String OutputName = args[2];
		
		if (args[0].equals("-e"))
			Encrypt(InputName, OutputName);
		
		if (args[0].equals("-d"))
			Decrypt(InputName, OutputName);
	}	
	
}
