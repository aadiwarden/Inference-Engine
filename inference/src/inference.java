import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class inference 
{
	
	class theTerm
	{
		String name = new String();
		ArrayList<String> para = new ArrayList<String>();
		
		public theTerm()
		{
			
		}
		
		public theTerm(theTerm temp)
		{
			this.name = temp.name;
			this.para.addAll(temp.para);
		}
		
	}

	class forEachClause
	{
		theTerm rhsTerm = new theTerm();
		ArrayList<theTerm> allTerms = new ArrayList<theTerm>();
		HashMap<String,String> hash = new HashMap<String,String>();
		
		public forEachClause()
		{
			
		}
		
		public forEachClause(forEachClause temp)
		{
			this.rhsTerm = new theTerm(temp.rhsTerm);
			for(theTerm term : temp.allTerms)
			{
				this.allTerms.add(new theTerm(term));
			}
			for(String str : temp.hash.keySet())
			{
				this.hash.put(str, temp.hash.get(str));
			}
			
		}
	}
	
	class theQue
	{
		String name;
		ArrayList<String> allPara = new ArrayList<String>();
	}
	
	int noOfQue, noOfCla;
	PrintWriter out1;
	PrintWriter out2;
	ArrayList<theTerm> theQueries;
	ArrayList<String> theFacts;
	ArrayList<String> toSub;
	ArrayList<String> claExists;
	ArrayList<String> FactsExists;
	HashMap<String,ArrayList<forEachClause>> toCheck = new HashMap<String,ArrayList<forEachClause>>();
	HashMap<String,ArrayList<theTerm>> toCheckFacts = new HashMap<String,ArrayList<theTerm>>();
	HashMap<String,ArrayList<theTerm>> loopCheck = new HashMap<String,ArrayList<theTerm>>();
	
	boolean isAVar(String check)
	{
		if(Character.isUpperCase(check.charAt(0)))
			return false;
		else
			return true;
	}
	
	
	boolean canConsider(theTerm rhsTerm1, theTerm pTerm1)
	{
		int i,count = pTerm1.para.size();
		String rhsStr1,pTermStr1;
		
		for(i = 0;i < count; i++)
		{
			rhsStr1 = rhsTerm1.para.get(i);
			pTermStr1 = pTerm1.para.get(i);
			if(!isAVar(pTermStr1) && !isAVar(rhsStr1))
			{
				if(!pTermStr1.equals(rhsStr1))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	boolean alreadyCrossed(theTerm pTerm)
	{
		int i,j;
		ArrayList<theTerm> aList = new ArrayList<theTerm>();
		
		if(loopCheck.containsKey(pTerm.name))
		{
			aList = loopCheck.get(pTerm.name);
			
			for(i = 0; i < aList.size() ; i++)
			{
				theTerm curTerm = aList.get(i);
				
				for(j = 0; j < curTerm.para.size(); j++)
				{
					String str = curTerm.para.get(j);
					
					if(!isAVar(str) || !isAVar(pTerm.para.get(j)))
					{
						if(!isAVar(str) && !isAVar(pTerm.para.get(j)))
						{
							if(!str.equals(pTerm.para.get(j)))
							{
								break;
							}
						}
						else
						{
							break;
						}
					}
				}
				if(j == curTerm.para.size())
				{
					return true;
				}
			}
		}
		else
		{
			return false;
		}
		
		return false;
	}
	
	ArrayList<theTerm> checkIfFact(theTerm pTerm)
	{
		int i,dontAdd = 0;
		String str;
		
		ArrayList<theTerm> aList = new ArrayList<theTerm>();
		ArrayList<theTerm> theFacts = new ArrayList<theTerm>();
		
		theFacts = toCheckFacts.get(pTerm.name);
		
		if(theFacts == null)
		{	
			return null;
		}
		
		for(theTerm curTerm : theFacts)
		{
			dontAdd = 0;
			
			for(i = 0 ;i < curTerm.para.size() ; i++)
			{
				str = pTerm.para.get(i);
				
				if(!isAVar(str))
				{
					if(!str.equals(curTerm.para.get(i)))
					{
						dontAdd = 1;
						break;
					}
				}
				else
				{
					continue;
				}
			}
			
			theTerm curTerm1 = new theTerm(curTerm);
			
			if(dontAdd == 0)
			{
				aList.add(curTerm1);		
			}
		}
		
		if(aList.size() == 0)
			return null;
		else
			return aList;
	}
	
	forEachClause changeEverything(forEachClause curCla, String from, String to)
	{
		int i,k;
		
		for(i = 0 ; i < curCla.rhsTerm.para.size(); i++)
		{
			if(curCla.rhsTerm.para.get(i).equals(from))
			{
				curCla.rhsTerm.para.remove(i);
				curCla.rhsTerm.para.add(i, to);
			}
		}
		
		ArrayList<theTerm> allTerms = curCla.allTerms;
	
		for(theTerm term : allTerms)
		{
			for(k = 0; k < term.para.size() ; k++)
			{
				if(term.para.get(k).equals(from))
				{
					term.para.remove(k);
					term.para.add(k, to);
				}
			}
		}

		return curCla;
	}
	
	boolean subs(forEachClause curCla, theTerm pTerm, int OrAnd)
	{
		int i,k;
		String str, temp, curParaStr;
		
		HashMap<String,String> Map = curCla.hash;
		HashMap<String,String> tempMap = new HashMap<String,String>();
		
		theTerm tempTerm;
		
		int count = pTerm.para.size();
		
		if(OrAnd == 1)
		{
			tempTerm = new theTerm(curCla.rhsTerm);
		}
		else
		{
			tempTerm = new theTerm(curCla.allTerms.get(0));
		}
		
		for(i = 0; i < count ; i++)
		{
			temp = tempTerm.para.get(i);
			curParaStr = pTerm.para.get(i);
			
			if(isAVar(curParaStr))
			{
				if(!tempMap.containsKey(curParaStr))
				{
					tempMap.put(curParaStr, temp);
				}
				
				else
				{
					if(!isAVar(temp))
					{
						tempMap.put(curParaStr, temp);
						continue; 
					}
					curCla = changeEverything(curCla,temp,tempMap.get(curParaStr));
				}
			}
		}
		
		for(i = 0 ; i < count; i++)
		{
			temp = tempTerm.para.get(i);
			curParaStr = pTerm.para.get(i);
			
			if(isAVar(temp) && !isAVar(curParaStr))
			{
				if(Map.containsKey(temp))
				{
					if(!curParaStr.equals(Map.get(temp)) && (Map.get(temp) != null))
					{
						
						return false;
					}
				}
				Map.put(temp, curParaStr);
			}
			else if(!isAVar(temp) && !isAVar(curParaStr))
			{
				if(!temp.equals(curParaStr))
				{
					return false;
				}
			}
			else
			{
				continue;
			}
		}
		
		count = curCla.rhsTerm.para.size();
		for(i = 0 ; i < count; i++)
		{
			if(isAVar(curCla.rhsTerm.para.get(i)))
			{
				str = curCla.rhsTerm.para.get(i);
				if(Map.containsKey(str))
				{
					curCla.rhsTerm.para.remove(i);
					curCla.rhsTerm.para.add(i, Map.get(str));
				}
			}
		}
		
		ArrayList<theTerm> allTerms = new ArrayList<theTerm>(curCla.allTerms);
		
		for(theTerm term : allTerms)
		{
			
			for(k = 0; k < term.para.size() ; k++)
			{
				if(Map.containsKey(term.para.get(k)))
				{
					String lostValue = term.para.get(k);
					term.para.remove(k);
					term.para.add(k, Map.get(lostValue));
				}
			}
		}
		
		return true;
	}
	
	ArrayList<theTerm> theORFunc(theTerm pTerm)
	{
		ArrayList<theTerm> ans = new ArrayList<theTerm>();
		
		HashMap<String,ArrayList<theTerm>> localLoopCheck = new HashMap<String,ArrayList<theTerm>>(loopCheck);
	
		int i,j,dontCall = 0;
		String rhsStr,pTermStr;
		ArrayList<theTerm> fromAnd = new ArrayList<theTerm>();
		
		if(checkIfFact(pTerm) != null)
		{
			ArrayList<theTerm> aList = new ArrayList<theTerm>(checkIfFact(pTerm));
			ans.addAll(aList);
		}
		
		if(alreadyCrossed(pTerm))
		{
			return ans;
		}
		else
		{
			ArrayList<theTerm> k;
			if(loopCheck.containsKey(pTerm.name))
			{
				k = loopCheck.get(pTerm.name);
			}
			else
			{
				k = new ArrayList<theTerm>();
			}
			k.add(pTerm);
			loopCheck.put(pTerm.name, k);
		}
		
		if(toCheck.containsKey(pTerm.name))
		{
			
			ArrayList<forEachClause> present = new ArrayList<forEachClause>(toCheck.get(pTerm.name));
			
			for(i = 0; i < present.size() ; i++)
			{
				if(canConsider(present.get(i).rhsTerm, pTerm))
				{
					HashMap<String,String> unify = new HashMap<String,String>();
					dontCall = 0;
					
					for(j = 0 ; j < pTerm.para.size() ; j++)
					{
						rhsStr = present.get(i).rhsTerm.para.get(j);
						pTermStr = pTerm.para.get(j);
						if(isAVar(rhsStr) && !isAVar(pTermStr))
						{
							if(unify.containsKey(rhsStr))
							{
								if(!pTermStr.equals(unify.get(rhsStr)) && (unify.get(rhsStr) != null))
								{
									dontCall = 1;
									break;
								}
							}
							unify.put(rhsStr, pTermStr);
						}
						else if(!isAVar(rhsStr) && !isAVar(pTermStr))
						{
							if(!rhsStr.equals(pTermStr))
							{
								dontCall = 1;
								break;
							}
						}
						else
						{
							continue;
						}
					}
					
					if(dontCall == 0)
					{
						forEachClause justToSend = new forEachClause(present.get(i));
						justToSend.hash = new HashMap<String,String>(unify);
						
						if(subs(justToSend, pTerm,1))
						{
							fromAnd = theANDFunc(justToSend);
						}
						
					}
					if(fromAnd.size() != 0)
					{
						ans.addAll(fromAnd);
					}
				}
			}
		}
		loopCheck = localLoopCheck;
		
		return ans;
	}
	
	ArrayList<theTerm> theANDFunc(forEachClause curCla)
	{	
		int i;
		ArrayList<theTerm> ans = new ArrayList<theTerm>();
		ArrayList<theTerm> allTerms = new ArrayList<theTerm>(curCla.allTerms);
		
		if(allTerms.size() == 0)
		{
			return null;
		}
		
		Queue<forEachClause> q = new LinkedList<forEachClause>();
		ArrayList<theTerm> aList = new ArrayList<theTerm>();
		
		forEachClause temp,copyOfTemp;
		
		q.add(curCla);
		
		while(!q.isEmpty())
		{
			
			aList.clear();
			temp = new forEachClause(q.remove());
			
			if(temp.allTerms.size() != 0)
			{
				
				theTerm justToSend = new theTerm(temp.allTerms.get(0));
				
				aList = theORFunc(justToSend);
				
				if(aList.size() != 0)
				{
					for(i = 0 ; i < aList.size() ; i++)
					{
						copyOfTemp = new forEachClause(temp);
						
						if(subs(copyOfTemp, aList.get(i),2))
						{
							forEachClause just = new forEachClause(copyOfTemp);
							just.allTerms.remove(0);
							q.add(just);
						}
					}
				}
			}
			else
			{
				ans.add(temp.rhsTerm);
			}
		}
		
		return ans;
	}

	boolean theBackwardChain() throws FileNotFoundException
	{
		int i;
		out1 = new PrintWriter("output.txt");
		out1.close();
		out2 = new PrintWriter(new FileOutputStream("output.txt",true));

		String toAdd;
		ArrayList<theTerm> listTempTerm;
		
		for(i = 0; i < theQueries.size() ; i++)
		{
			
			loopCheck.clear();
			theTerm toSend = new theTerm(theQueries.get(i));
			
			ArrayList<theTerm> ans = new ArrayList<theTerm>();
			
			if(i != 0 )
			{
				out2.append("\n");
			}
			
			try
			{
				ans = theORFunc(toSend);
			}
		
			catch(Exception e)
			{
				out2.append("FALSE");
				continue;
			}
			
			if(ans.size() == 0)
			{
				out2.append("FALSE");
			}
			else
			{
				out2.append("TRUE");
				
				toAdd = ans.get(0).name;
				
				if(toCheckFacts.containsKey(toAdd))
				{
					listTempTerm = toCheckFacts.get(toAdd);
				}
				else
				{
					listTempTerm = new ArrayList<theTerm>();
				}
				
				listTempTerm.add(ans.get(0));
				toCheckFacts.put(toAdd, listTempTerm);
			}
		}
		
		out2.close();
		return false;
	}
	
	void display()
	{
		int i,j,k;
		
		System.out.println("The Queries are : ");
		for(j = 0;j< theQueries.size(); j++)
		{
			System.out.print( theQueries.get(j).name +" : ");
			for(k = 0; k < theQueries.get(j).para.size(); k++)
			{
				System.out.print(theQueries.get(j).para.get(k) + " , ");
			}
			System.out.println("");
		}
		
		
		System.out.println("\nThe Facts are : ");
		
		for(String key : toCheckFacts.keySet())
		{
			ArrayList<theTerm> allTheTerms = new ArrayList<theTerm>();
			allTheTerms = toCheckFacts.get(key);
			for(j = 0;j< allTheTerms.size(); j++)
			{
				System.out.print( allTheTerms.get(j).name +" : ");
				for(k = 0; k < allTheTerms.get(j).para.size(); k++)
				{
					System.out.print(allTheTerms.get(j).para.get(k) + " , ");
				}
				System.out.println("");
			}			
		}
		
		System.out.println("\nThe Clauses are : ");
		
		for(String key : toCheck.keySet())
		{
			ArrayList<forEachClause> toDisplay = new ArrayList<forEachClause>();
			toDisplay = toCheck.get(key);
			
			
			System.out.println("The Keys is: " + key);
			System.out.print("Parameters : ");
			
			
			for(i = 0;i < toDisplay.size() ; i++)
			{
				ArrayList<theTerm> allTheTerms = new ArrayList<theTerm>();
				for(String str : toDisplay.get(i).rhsTerm.para)
				{
					System.out.println(str + " , ");
				}
				
				allTheTerms = toDisplay.get(i).allTerms;
				
				for(j = 0;j< allTheTerms.size(); j++)
				{
					System.out.print( allTheTerms.get(j).name +" : ");
					for(k = 0; k < allTheTerms.get(j).para.size(); k++)
					{
						System.out.print(allTheTerms.get(j).para.get(k) + " , ");
					}
					System.out.println("");
				}
			}
		}
	}
	
	void readTheInput(String theFile) throws NumberFormatException, FileNotFoundException
	{
		int i,j,k;
		
		theTerm tempTerm  = new theTerm();
		ArrayList<String> arrList;
		String nextline, pre, preTemp,str;
		String args[], claSplit[], termSplit[], preArgs[], preArgsTemp[];
		
		File file1 = new File(theFile);
		Scanner ob = new Scanner(file1);
		
		FactsExists = new ArrayList<String>();
		claExists = new ArrayList<String>();
		theQueries = new ArrayList<theTerm>();
		theFacts = new ArrayList<String>();
		
		noOfQue = Integer.parseInt(ob.nextLine());
		
		for(i = 0; i < noOfQue ; i++)
		{
			tempTerm  = new theTerm();
			str = ob.nextLine();
			termSplit = str.split("\\(");
			pre = termSplit[0];
			termSplit = termSplit[1].split("\\)");
			preArgs = termSplit[0].split(",");
			tempTerm.name = pre;
			arrList = new ArrayList<String>(Arrays.asList(preArgs));
			tempTerm.para = arrList;
			
			theQueries.add(tempTerm);
		}
		
		noOfCla = Integer.parseInt(ob.nextLine());
		
		for(i = 0; i < noOfCla ; i++)
		{
			tempTerm  = new theTerm();
			nextline = ob.nextLine();
			claSplit = nextline.split(" => ");
			
			if(claSplit.length == 1)
			{
				ArrayList<theTerm> listTempTerm;
				
				termSplit = claSplit[0].split("\\(");
				pre = termSplit[0];
				termSplit = termSplit[1].split("\\)");
				preArgs = termSplit[0].split(",");
				tempTerm.name = pre;
				arrList = new ArrayList<String>(Arrays.asList(preArgs));
				tempTerm.para = arrList;
				
				
				
				if(toCheckFacts.containsKey(pre))
				{
					listTempTerm = toCheckFacts.get(pre);
				}
				else
				{
					listTempTerm = new ArrayList<theTerm>();
				}
				
				if(FactsExists.contains(nextline))
				{
					
				}
				else
				{
					FactsExists.add(nextline);
					listTempTerm.add(tempTerm);
					toCheckFacts.put(pre, listTempTerm);
				}
			}
			else
			{
				
				args = claSplit[0].split(" \\^ ");
				
				ArrayList<forEachClause> cur;
				termSplit = claSplit[1].split("\\(");
				pre = termSplit[0];
				termSplit = termSplit[1].split("\\)");
				preArgs = termSplit[0].split(",");
				
				if(toCheck.containsKey(pre))
				{
					cur = toCheck.get(pre);
				}
				else
				{
					cur = new ArrayList<forEachClause>();
				}
				
				arrList = new ArrayList<String>(Arrays.asList(preArgs));
				forEachClause fec = new forEachClause();
				fec.rhsTerm.name = pre;
				fec.rhsTerm.para.addAll(arrList);
								
				for(j = 0; j < args.length ; j++)
				{
					tempTerm  = new theTerm();
					
					termSplit = args[j].split("\\(");
					preTemp = termSplit[0];
					termSplit = termSplit[1].split("\\)");
					preArgsTemp = termSplit[0].split(",");
					
					theTerm curTerm = new theTerm();
					curTerm.name = preTemp;
					
					curTerm.para = new ArrayList<String>();
					
					
					for(k = 0; k < preArgsTemp.length ; k++)
					{
						curTerm.para.add(k, preArgsTemp[k]);
						
					}
					fec.allTerms.add(curTerm);	
				}
				
				
				if(claExists.contains(nextline))
				{
					
				}
				else
				{
					cur.add(fec);
					claExists.add(nextline);
					toCheck.put(pre,cur);
				}
			}
		}
		
		theBackwardChain();
		ob.close();
	}
	
	
	public static void main(String args[]) throws FileNotFoundException
	{
		inference inferenceOb = new inference();
		inferenceOb.readTheInput(args[1]);
	}
}
