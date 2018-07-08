package cop5556sp18;

import java.util.HashMap;
import java.util.LinkedList;

import cop5556sp18.AST.Declaration;

public class SymbolTable {
	
	private class Symbol{
		Integer scope;
		Declaration dec;
		
		public Symbol(Integer scope, Declaration dec) {
			this.scope = scope;
			this.dec = dec;
		}
	}
	
	HashMap<String, LinkedList<Symbol>> st;

	public SymbolTable() {
		st = new HashMap<String, LinkedList<Symbol>>();
	}
	
	public Declaration lookup(String iden, LinkedList<Integer> scope_stack){
		if(st.containsKey(iden)){
			LinkedList<Symbol> li = st.get(iden);
			for(Integer i : scope_stack) {
				for(Symbol j : li) {
					if(j.scope.equals(i)) {
						return j.dec;
					}
				}
			}
			return null;
		}
		else
			return null;		
	}
	
	public void insert(String iden, Integer scope, Declaration dec) {
		Symbol sym = new Symbol(scope,dec);
		LinkedList<Symbol> li;
		if(st.containsKey(iden)) {
			li = st.get(iden);
		}
		else {
			li = new LinkedList<Symbol>();
			st.put(iden, li);
		}	
		li.addFirst(sym);
	}
	
	public boolean existInScope(String iden, Integer scope) {
		if(st.containsKey(iden)) {
			LinkedList<Symbol> li = st.get(iden);
			for(Symbol s : li) {
				if(s.scope.equals(scope))
					return true;
			}
			return false;
		}
		else {
			return false;
		}
	}
}
