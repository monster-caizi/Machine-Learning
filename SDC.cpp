#include<iostream>
#include <string>
#include <cstring>
#include <fstream>
#include <math.h>
#include <queue>
#include <map>
#include<time.h>

using namespace std;


#define Dimension 2

struct node{
	double myVector[Dimension];// = new double[100];
	double myData;
};
struct neighbor;
struct link{
	node * dataNode;
	link * next;
	neighbor *neghboriList;
	int neighborNum;
	int cluster;
	
};
struct neighbor
{
	link *data;
	double distance;
	neighbor  *next;
};
struct clusterNode
{
	link *data;
	clusterNode * next;
};
struct clusterLink
{
	clusterNode *data;//add a tail;
	//int level;
	int cluster;
	clusterLink *next;
	//int index;
	//bool first;
	node center;
	int clusterNum;
};
struct XNode
{
	link *data;
	XNode * next;
	//bool flag;
	double distance;
};

class SDC{
private:
	double eps;
	int Minpt;
	double dEps;
	link *part;
	clusterLink * clusters;
	XNode * x;
	string outFile;
public:
	SDC(int m,double e,string out)
	{
		this->eps = e;
		this->Minpt = m;
		this->dEps = 0.5*eps;
		this->outFile = out;
		this->part = 0;
		this->clusters = 0;
		this->x = 0;
	}
	
	~SDC()
	{
		ofstream file;               //定义输出文件
		file.open(outFile.data());
		while (x != 0)
		{
			XNode *delX = x;
			x = x->next;
			delete delX;
		}

		while (clusters!=0)
		{
			clusterNode *myClusterNode = clusters->data;
			while(myClusterNode!=0)
			{
				clusterNode  *myNode = myClusterNode;
				myClusterNode = myClusterNode->next;
				delete myNode;
			}
			clusterLink *myClusterLinkP = clusters;
			clusters = clusters->next;
			delete myClusterLinkP;
		}
		//int d = 1;
		while (part !=0 )
		{
			while ( part->neghboriList!=0)
			{
					neighbor * myNeighborList = part->neghboriList;
					part->neghboriList = part->neghboriList->next;
					delete myNeighborList;
			}
			if(part->dataNode!=0)
			{
				file<<part->dataNode->myData;
				for(int i=0;i<Dimension;i++)
					file<<"\t"<<part->dataNode->myVector[i];
				//int a = ;
				file<<"\t"<<part->cluster - 10*(part->cluster /10)<<endl;
				delete part->dataNode;
			}
			link *node = part;
			part= part->next;
			delete node;
		}
	}

	void readData(string fileName){
		ifstream file;               //定义输出文件
		file.open(fileName.data());      //将文件流对象与文件连接起来  
		//assert(file.is_open());   //若失败,则输出错误消息,并终止程序运行 
		double data;
		link *dataLink = new link,*p;
		dataLink->dataNode = 0;
		dataLink->cluster = 0;
		dataLink->neighborNum = 0;
		dataLink->next = 0;
		
		p = dataLink;
		while(file>>data)
		{
  			node *dataNode = new node;
			dataNode->myData = data;
			
			double aa;
			//for(int j=0;j<Dimension;j++) file>>aa;
			//dataNode->myVector[0] = data;
			for(int i=0;i<Dimension;i++) 
				file>>dataNode->myVector[i];
			file>> aa;
			//file>>dataNode->myData;
			link *linkNode = new link;
			
			linkNode->dataNode = dataNode;
			linkNode->neghboriList = 0;
			linkNode->neighborNum =0;
			linkNode->cluster =  0;
			linkNode->next = 0;
			
			p->next = linkNode;
			p = p->next;
		}
		part = dataLink->next;
		delete dataLink;
	}

	double calculateVector(double node1[],double node2[])
	{
		//calculate Euclidean Distance
		double a=0;//,b=0,c=0;
		for(int i =0;i<Dimension;i++)
		{
			a += ((node1[i]-node2[i])*(node1[i]-node2[i]));
			//b += node1[i]*node1[i];
			//c += node2[i]*node2[i];
		}
		return sqrt(a);
	}

	//eps can be optimized
	void DBSCANpartPre()
	{
		link *list = part;
		while (list!=0)
		{
			link *pRemain = list->next;
			neighbor *dataNeighbor1 = 0, *dataNeighbor2 = 0;
			while(pRemain!=0)
			{
				double distance = calculateVector(list->dataNode->myVector,pRemain->dataNode->myVector);
				if(distance<eps){
					dataNeighbor1 = new neighbor;
					dataNeighbor1->data = pRemain;
					dataNeighbor1->distance = distance;
					dataNeighbor1->next = list->neghboriList;
					list->neghboriList = dataNeighbor1;
					list->neighborNum ++;

					dataNeighbor2 = new neighbor;
					dataNeighbor2->data = list;
					dataNeighbor2->distance = distance;
					dataNeighbor2->next = pRemain->neghboriList;
					pRemain->neghboriList = dataNeighbor2;
					pRemain->neighborNum ++;
				}
				pRemain = pRemain->next;
			}
			list = list->next;
		}
		return ;
	}

	void DBSCANpart()
	{
		link *list = part;
		clusterLink *clusterList = 0;
		int cluster = 0 ;
		while (list!=0)
		{
			if(list->neighborNum>Minpt&&list->cluster==0)
			{
				cluster++;
				clusterNode *dataNode = new clusterNode,*pNode = 0;
				dataNode->data = list;
				dataNode->next = 0;
				dataNode->data->cluster = cluster;
				clusterLink *nextNode = new clusterLink;
				nextNode->clusterNum = 1;
				nextNode->data = dataNode;
				nextNode->next =clusterList;
				nextNode->cluster = cluster;
				clusterList = nextNode;
				link *myData = list;
				if(pNode == 0)
				{
					pNode = dataNode;
				}else					
				{
					pNode->next = new clusterNode;
					nextNode->clusterNum ++;
					pNode = pNode->next;
					pNode->data = myData;
					pNode->next = 0;
				}					
				neighbor *myNei = myData->neghboriList;
				int number = 0;
				while (myNei!=0)
				{
					if(myNei->data->cluster==0)
					{
						myNei->data->cluster = cluster;
						pNode->next = new clusterNode;
						nextNode->clusterNum ++;
						pNode = pNode->next;
						pNode->data = myNei->data;
						pNode->next = 0;
						number ++;
					}
					myNei = myNei->next;
				}
				if(number < Minpt)
				{
					cluster--;
					clusterLink *delN = clusterList;
					clusterList = clusterList->next;
					delete delN;
				}
			}else   if(list->cluster == 0)
			{
				XNode *no = new XNode;
				no->data = list;
				//no->flag = false;
				no->next = x;
				x = no;

			}
	
			while ( list->neghboriList!=0)
			{
					neighbor * myNeighborList = list->neghboriList;
					list->neghboriList = list->neghboriList->next;
					delete myNeighborList;
			}
			list = list->next;
		}
		clusters = 0;
		if(clusterList != 0 )
			while(clusterList!=0)
			{
				clusterLink  *node = clusterList->next;
				clusterList->next = clusters;
				clusters = clusterList;
				clusterList = node;
			}
		
	}

	void expandCluster()
	{
		clusterLink *pC = clusters;
		while (pC != 0)
		{
			//calculate centers
			if (x == 0)
				break;
			node pMax,pMin;
			for(int i = 0;i<Dimension;i++)
			{
				pMax.myVector[i] = -1;
				pMin.myVector[i] = 10000;
			}
			clusterNode *p = pC->data; 
			while(p!=0)
			{
				for(int i=0; i<Dimension; i++)
				{
					if(p->data->dataNode->myVector[i] > pMax.myVector[i])
						pMax.myVector[i] = p->data->dataNode->myVector[i];
					if(p->data->dataNode->myVector[i] < pMin.myVector[i])
						pMin.myVector[i] = p->data->dataNode->myVector[i];
				}
				
				p = p->next;
			}
			pC->center.myData = 1000;
		
			for(int i=0; i<Dimension; i++)
				pC->center.myVector[i] = (pMax.myVector[i]-pMin.myVector[i])/2 + pMin.myVector[i];

			//expand cluster
			XNode *pX = x;
			while(pX != 0)
			{
				pX->distance = calculateVector(pC->center.myVector,pX->data->dataNode->myVector);
				pX = pX->next;
			}

			bool flag = true;
			double myEps = this->eps;
			while(1)
			{
				myEps = myEps + dEps;
				pX = x;
				XNode *lp = 0;
				while(pX != 0)
				{
					if( pX->distance <= myEps)
					{
						flag = false;
						
						clusterNode *na = new clusterNode;
						
						na->data = pX->data;
						pX->data->cluster = pC->cluster;
						na->next = pC->data;
						pC->data = na;
						pC->clusterNum ++;
						if(lp ==0)
						{
							x = x->next;
							delete pX;
							pX = x;
						}
						else
						{
							lp->next = pX->next;
							delete pX;
							pX = lp->next;
						}
						
					}else
					{
						lp = pX;
						pX = pX->next;
					}
				}
				if(flag)
					break;
				flag = true;
			}

			//next cluster
			pC = pC->next;
		}
	}

	void coutCluterNum()
	{

		clusterLink *p = clusters,*ph = 0;//clusterNode;
		while(p!=0)
		{
			/*clusterNode *np = p->data;
			int a =0;
			while(np !=0)
			{
				a++;
				np = np->next;		
			}
			if(a>0)
			{
				cout<<"The cluster is "<<p->cluster<<" .The cluster Number is "<<a<<endl;
				ph = p;
			}*/
//			cout<<"The cluster is "<<p->cluster<<" .The cluster Number is "<<p->clusterNum<<endl;
			cout<<p->clusterNum<<',';
			p = p->next;
		}
		cout<<endl;
	}

	void changeClusterNum()
	{
		clusterLink *cNode = clusters;
		while(cNode->next !=0)
		{
			if(cNode->next->clusterNum >100)
			{
				clusterLink *eNode = cNode->next->next;
				cNode->next->next = clusters;
				clusters = cNode->next;
				cNode->next = eNode;
			}else
				cNode = cNode->next;
		}
		cNode = clusters;
		int clusterNumber = 0;
		while(cNode != 0)		
		{
			clusterNumber ++;
			cNode->cluster  = clusterNumber;
			clusterNode *n = cNode->data ;
			while (n != 0)
			{
				n->data->cluster = clusterNumber;
				n = n->next;
			}
			cNode = cNode->next;
		}

	}
};



int main(){
	
	clock_t start,finish;
	double totaltime;
	start=clock();

	cout<<"BEGIN!!!! \n";
	SDC *mySDC = new SDC(4,1.26,"E:/SDC-test.txt");

	mySDC->readData("E:/data-outer2.txt");
	mySDC->DBSCANpartPre();
	mySDC->DBSCANpart();
	mySDC->expandCluster();
	mySDC->changeClusterNum();
	mySDC->coutCluterNum();


	cout<<"DONE!!!! \n";
	
	finish=clock();

	totaltime=(double)(finish-start)/CLOCKS_PER_SEC;
	cout<<"\nTime of this programe is "<<totaltime<<" s!"<<endl;
	
	
	delete mySDC;


	return 0;
}
