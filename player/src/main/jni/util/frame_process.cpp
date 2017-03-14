#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "frame_process.h"

#define YUV

#ifdef __cplusplus
extern "C" {
#endif

#include <math.h>

#define DET_MID_EDGE_EN         1

//detect edge in middle
#define MID_DETECTED_HALF_SIZE	(8)

//rules for 2D/3D
#define FMT_2D_3D_TH		(1.5)
#define HIST_DIFF_TH0		(250)
#define HIST_DIFF_2D_TH		(400)


//detect hist diff of left & right.
//detect region & steps
#define DET_H_DEN       4   //Detect region denominator
#define DET_H_TOP       1
#define DET_H_BOT       3
#define DET_LINE_INTVL  2

#define FRAME_MEAN_VALID_MIN    4
#define FRAME_VAR_VALID_MIN     5
#define FRAME_VAR_VALID_TH      10
#define FORAT2D_DIFF_TH         150

#define DIS_RATIO       1024



using namespace std;


//mid edge alg
// SORT_DESCENDING;
#define SWAP(a, b)	(tmp = a, a = b, b = tmp)
int* vecSort(int *pSrc, int lenth)
{
	int *pTmp = new int[lenth];
	memcpy(pTmp, pSrc, lenth * sizeof(int));
	int i, j;
	int tmp;

	for (i = 0; i < lenth - 1; i++)
	{
		for (j = i + 1; j < lenth; j++)
		{
			if (pTmp[i] < pTmp[j])
			{
				SWAP(pTmp[i], pTmp[j]);
			}
		}
	}
	return pTmp;
}

int vecMid(int *pSrc, int lenth)
{
	int midIdx = (lenth + 1) / 2 - 1;	//4-->1, 5-->2;
	int midVal;

	int *pVec;

	pVec = vecSort(pSrc, lenth);
	midVal = pVec[midIdx];

	delete pVec;

	return midVal;
}

float procVec(int *p, int halfSize)
{
	int i;
	int j;

	int *pVecMid;
	int midPeak;

	pVecMid = new int[4];
	for (i = halfSize - 2, j = 0; i < halfSize + 2; i++, j++)
		pVecMid[j] = p[i];

	midPeak = vecMid(pVecMid, 4);
	delete pVecMid;

	int *pVec0, *pVec1;
	pVec0 = new int[halfSize];
	pVec1 = new int[halfSize];
	memcpy(pVec0, p, halfSize * sizeof(int));
	memcpy(pVec1, &p[halfSize], halfSize * sizeof(int));

	int bkMid0, bkMid1;
	bkMid0 = vecMid(pVec0, halfSize);
	bkMid1 = vecMid(pVec1, halfSize);

	float metric;

	metric = (float)midPeak * 2.0 / (bkMid0 + bkMid1);

//	cout << "mid: " << bkMid0 << ", " << bkMid1 << ", midPeak: " << midPeak << ", ";
//	cout << "ratio: " << metric << endl;

	return metric;
}

/*	detect horizental edge
{	{-1, 0, 1};
{-2, 0, 2};
{-1, 0, 1};
}
*/

ushort * procSobel(uchar *pY, int w, int h, int procHalfLen)
{
	const short hSobel[3][3] = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
	ushort *pSbl;
	int mid;
	mid = w / 2;
	int i, j, jj;
	int ww;

	ww = 2 * procHalfLen;
	pSbl = new ushort[h * ww];

	uchar *pT, *pM, *pB;

	for (i = 0; i < h; i++)
	{
		int t, b;
		t = MAX(0, (i - 1));
		b = MIN(h - 1, (i + 1));

		pT = &pY[t*w];	pB = &pY[b*w];	pM = &pY[i*w];

		for (j = mid - procHalfLen, jj = 0; j < mid + procHalfLen; j++, jj++)
		{
			short sum;
			sum = pT[j - 1] * hSobel[0][0] + pT[j] * hSobel[0][1] + pT[j + 1] * hSobel[0][2];
			sum += pM[j - 1] * hSobel[1][0] + pM[j] * hSobel[1][1] + pM[j + 1] * hSobel[1][2];
			sum += pB[j - 1] * hSobel[2][0] + pB[j] * hSobel[2][1] + pB[j + 1] * hSobel[2][2];

			pSbl[i*ww + jj] = ABS(sum);
		}
	}

	return pSbl;
}

int *procVPrjt(ushort *pImg, int w, int h)
{
	int i, j;

	int *pPrj;

	pPrj = new int[w];

	for (j = 0; j < w; j++)
	{
		pPrj[j] = 0;
		for (i = 0; i < h; i++)
		{
			pPrj[j] += pImg[i*w + j];
		}
	}

	return pPrj;
}


//////////////////////////////////////////////////////////////
//hist diff alg

/*
nHistShift: 0x4 if hist is 16 bin,
0x3 if hist is 32 bin.
*/
static void lineHistAcumulate(uchar *pY, int w, int *pHist, int nHistShift)
{
	int i = 0;
	int idx;
	for (i = 0; i<w; i++, pY++)
	{
		idx = (*pY) >> nHistShift;
		pHist[idx]++;
	}
}

static int histCalc(uchar *pY, int *pHist, int nHistLen,
	int w, int h,
	int nLineSize, int nHIntvl,
	int nHBegin, int nHEnd)
{
	int i;
	int nStep;

	memset(pHist, 0, sizeof(int)*nHistLen);

	//convert from hist bin number to step
	for (i = 0; i <= 8; i++)
	{
		if (nHistLen == (1 << i))
			break;
	}

	if (i > 8)
		return -1;
	else
		nStep = 8 - i; //get 0x3 if i==5.

	pY += nLineSize*nHBegin;
	for (i = nHBegin; i<nHEnd; i = i + nHIntvl)
	{
		lineHistAcumulate(pY, w, pHist, nStep);
		pY += nLineSize*nHIntvl;
	}

	return 0;
}


//get Variance of hist
static int histVarianceMean(int *pHist, int nLen, int *pMean)
{
	int nTotal = 0;     //total pixel number
	int nMean = 0;      //avg y value of
	int sum = 0;

	int i;
	int *pOrg;
	int var;

	pOrg = pHist;

	for (i = 0; i<nLen; i++, pHist++)
	{
		sum += (*pHist)*i;
		nTotal += (*pHist);
	}
	nMean = sum / nTotal;

	sum = 0;
	pHist = pOrg;
	// get variantce.
	for (i = 0; i<nLen; i++, pHist++)
	{
		sum += (nMean - i)*(nMean - i)*(*pHist);
	}
	var = sum / nTotal;

	//for 1080p, nTotal = 1920x1080 = 2,073,600,
	//and sum_max when 32bins is uniform distribute, i.e., 1920x1080/32 = 64,800 for each bin
	//nMean = 15,   Sum[(0-15)^2 *64800 + (1-15)^2 *64800 + ... + (31-15)^2 *64800]= 177292800
	// var max = 177292800/(1920x1080) = 85;
	// note: this value is in-depence with resolution, only related to distribution & hist bin number

	//if (var < 0)
	//	ALOGE("Abnormal var %d: nMean %d, sum %d, total %d ", var, nMean, sum, nTotal);

	*pMean = nMean;

	return var;
}

//get distance of hist.   sum( abs(p0[i] - p1[i])) / total
static int histDistance(int *p0, int *p1, int nLen)
{
	int diff[256];
	int nTotal = 0;     //total pixel number

	int i;
	int sum = 0;
	int dist;

	for (i = 0; i<nLen; i++, p0++, p1++)
	{
		diff[i] = *p0 - *p1;
		nTotal += (*p0);

		//
		sum += ABS(diff[i]);
	}

	dist = ((sum * 16 ) / nTotal) * ( DIS_RATIO/16);

	//for 1080p, nTotal = 1920x1080 = 2,073,600,
	//and sum_max when two 32bins hist not overlap.
	// sum max = 1920x1080 + 1920x1080,
	//        and dist max = 2*1920x1080/(1920x1080/DIS_RATIO) = 2*DIS_RATIO.
	// note: this value is in-depence with resolution, only related to distribution

	return dist;
}

static int videoHistStat(uchar *pY, int w, int h, int nLineSize,
	int *pLHist, int *pRHist, int nHistLen)
{
	uchar *pLeft, *pRight;
	int nDetTop, nDetBottom, nDetIntvl;
	int nPixelCnt;

	w = w / 2;
	pLeft = pY;
	pRight = pLeft + w;

	//ALOGI("Frame info: %6dx%6d, linesize %d, %p, %p", w, h, frame->linesize[0], pLeft, pRight);

	nDetTop = h*DET_H_TOP / DET_H_DEN;
	nDetBottom = h*DET_H_BOT / DET_H_DEN;
	nDetIntvl = DET_LINE_INTVL;

	nPixelCnt = ((nDetBottom - nDetTop) / nDetIntvl)*w;

	histCalc(pLeft, pLHist, nHistLen,
		w, h,
		nLineSize, nDetIntvl,
		nDetTop, nDetBottom);
	histCalc(pRight, pRHist, nHistLen,
		w, h,
		nLineSize, nDetIntvl,
		nDetTop, nDetBottom);

	return nPixelCnt;
}

static bool videoStatValid(int nLVar, int nRVar, int nLMean, int nRMean, int nDiff)
{
	//only frame is bright enough, or L/R different enough, it can be regard as valid frame
	if (((nLVar > FRAME_VAR_VALID_TH) && (nLMean > FRAME_MEAN_VALID_MIN)) ||
		((nRVar > FRAME_VAR_VALID_TH) && (nRMean > FRAME_MEAN_VALID_MIN)) ||
		((nDiff > FORAT2D_DIFF_TH) && (nLVar > FRAME_VAR_VALID_MIN) && (nRVar > FRAME_VAR_VALID_MIN)))
	{
		return true;
	}
	else
	{
		LOGD("VIDEO detect. Invalid:  var = %d, %d, mean = %d, %d, diff = %d",
			nLVar, nRVar, nLMean, nRMean, nDiff);
		return false;
	}
}

//////////////////////////////////////////////////////////////////////
static __inline int RGBToY(uchar r, uchar g, uchar b) {
	return (66 * r + 129 * g + 25 * b + 0x1080) >> 8;
}

//ABGR8888
static void abgr8888CvtYInt(uchar *pAbgr, int w, int h, uchar *pY)
{
	int row, c;
	uchar r, g, b;

	int bpp = 4;

	int rch, gch, bch, ach;
	rch = 0; gch = 1; bch = 2; ach = 3;

	int y;

	for (row = 0; row < h; row++)
	{
		for (c = 0; c < w; c++)
		{
			r = pAbgr[(row * w + c) * bpp + rch];
			g = pAbgr[(row * w + c) * bpp + gch];
			b = pAbgr[(row * w + c) * bpp + bch];

			y = RGBToY(r, g, b);

			pY[row * w + c] = y;
		}
	}
}


////////////////////////////////////////////////////////////////////
// decide logic

// 0: 2d.  1: 3d. 2: TBD
int sbs3DRecognition(float midEgdeMetric, int nLVar, int nRVar, int nLMean, int nRMean, int nDiff)
{
	//
	int res = 0;

	//Below is 2D
	//1. hist diff is very large
	//2. hist diff is large, & can't find mid edge,
#if DET_MID_EDGE_EN
	if (((nDiff >  HIST_DIFF_2D_TH)) ||
		((midEgdeMetric < FMT_2D_3D_TH) && (nDiff >  HIST_DIFF_TH0)))
	{
		res = 0;
	}
#else
    if ((nDiff >  HIST_DIFF_TH0))
    {
        res = 0;
    }

#endif
	else
	{
		res = 1;
	}

	return res;
}

//NOTE: it suppose input is ABGR888
int isSBSFrame(uchar* pixels, uint32_t w, uint32_t h, int bpp)
{
	uchar *pY;
	float metric;

	pY = new uchar[w * h];
	abgr8888CvtYInt(pixels, w, h, pY);

#if DET_MID_EDGE_EN
	////////////////////////////////////////
	//alg.1 edge of middle
	int halfSize = MID_DETECTED_HALF_SIZE;
	ushort *pSbl;
	pSbl = procSobel(pY, w, h, halfSize);

	int *pVPrj;
	pVPrj = procVPrjt(pSbl, 2 * halfSize, h);
	//priVec(pVPrj, halfSize);
	metric = procVec(pVPrj, halfSize);
	delete pSbl;
	delete pVPrj;
#endif

	////////////////////////////////////////
	//alg.2 hist distance
	int aLhist[32], aRhist[32];
	int nLVar, nRVar;
	int nLMean, nRMean;
	int nDiff;
	int nLineSize = w;

	videoHistStat(pY, w, h, nLineSize, aLhist, aRhist, 32);
	nLVar = histVarianceMean(aLhist, 32, &nLMean);
	nRVar = histVarianceMean(aRhist, 32, &nRMean);
	nDiff = histDistance(aLhist, aRhist, 32);

	//cout << "mid edge ratio: " << metric << ".  hist Diff: " << nDiff << endl;

	int res;
	res = sbs3DRecognition(metric, nLVar, nRVar, nLMean, nRMean, nDiff);

	return res;
}



#ifdef __cplusplus
}
#endif
