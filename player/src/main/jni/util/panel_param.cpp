#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "panel_param.h"

float m_cover = (float)0.6;
float m_cot = (float)0.3;
float m_offset = (float)0;
bool m_slope = true;
bool m_redFirst = false;	//subpixel sequence: r, g, b. Otherwise is b, g, r


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
typedef struct float3 {
	float x;
	float y;
	float z;
} float3;

typedef struct float2 {
	float x;
	float y;
} float2;

static float lerp(float a, float b, float t)
{
	return a + t*(b - a);//a * (1.0 - t) + (b * t);
}

static float frac(float X)
{
	return X - (int)X;
}

static float3 DoIndex(float2 pos, float nview, float cot, float cover, float offset)
{
	float tmp;
	float3 index;
	tmp = 0.0;

	cot = -cot;

	index.x = (3.0*cot*(tmp + pos.y) + offset + 3.0*pos.x + 2.0) / cover;
	index.y = (3.0*cot*(tmp + pos.y) + offset + 3.0*pos.x + 1.0) / cover;
	index.z = (3.0*cot*(tmp + pos.y) + offset + 3.0*pos.x + 0.0) / cover;
	index.x -= floor(index.x);
	index.y -= floor(index.y);
	index.z -= floor(index.z);
	index.x *= nview;
	index.y *= nview;
	index.z *= nview;

	return index;
}

static unsigned char* slopeTempleteGen(unsigned char* buf, int nview, int Tw, int Th)
{
	int i, j;

	unsigned char* res = buf;
	if (!res) return 0;

	float cot, cover, offset;

	cot = m_cot;
	cover = m_cover;
	offset = m_offset;

	float2 pos;
	for (j = 0; j<Th; j++)
	{
		for (i = 0; i<Tw; i++)
		{
			pos.x = i;
			pos.y = j;
			float3 index = DoIndex(pos, nview, cot, cover, offset);

			float3 delta;
			delta.x = 1.0 - frac(index.x);
			delta.y = 1.0 - frac(index.y);
			delta.z = 1.0 - frac(index.z);

			index.x = floor(index.x);
			index.y = floor(index.y);
			index.z = floor(index.z);

			float3 index1;
			index1.x = fmod(index.x + 1.0, nview); index1.x = index1.x >= 3.0 ? 1.0 : 0.0;
			index1.y = fmod(index.y + 1.0, nview); index1.y = index1.y >= 3.0 ? 1.0 : 0.0;
			index1.z = fmod(index.z + 1.0, nview); index1.z = index1.z >= 3.0 ? 1.0 : 0.0;

			index.x = index.x >= 3.0 ? 1.0 : 0.0;
			index.y = index.y >= 3.0 ? 1.0 : 0.0;
			index.z = index.z >= 3.0 ? 1.0 : 0.0;

			unsigned char tr0 = (index.x == 1.0) ? 0 : 255;
			unsigned char tg0 = (index.y == 1.0) ? 0 : 255;
			unsigned char tb0 = (index.z == 1.0) ? 0 : 255;

			unsigned char tr1 = (index1.x == 1.0) ? 0 : 255;
			unsigned char tg1 = (index1.y == 1.0) ? 0 : 255;
			unsigned char tb1 = (index1.z == 1.0) ? 0 : 255;
			//[t0, tx, t1],   tx --> t1 = delta.
			// tx = t1 * (1.0 - delta) + (t0 * delta);  lerp()
			//		delta = 0, tx = t1;  delta = 1, tx = t0
			//x is 2 ch, it is for red ch
			int rr;
			if (tr1 != tr0)
				rr = lerp((float)tr1, (float)tr0, delta.x);
			else
				rr = tr1;

			int rg;
			if (tg1 != tg0)
				rg = lerp((float)tg1, (float)tg0, delta.y);
			else
				rg = tg1;

			//z is 0 ch, it is for blue ch
			int rb;
			if (tb1 != tb0)
				rb = lerp((float)tb1, (float)tb0, delta.z);
			else
				rb = tb1;

			res[j*Tw * 3 + i * 3] = rr;
			res[j*Tw * 3 + i * 3 + 1] = rg;
			res[j*Tw * 3 + i * 3 + 2] = rb;

		}
	}
	return res;
}


static unsigned char* vertTempleteGen(unsigned char* buf, int count, int Tw, int Th )
{
  int i,j;

  int nview = count;
  unsigned char* res = buf;
  if(!res) return 0;

  for(j = 0;j<Th;j++)
  {
    for(i = 0;i<Tw;i++)
	{
      int odd_even;
      if(i % 2) odd_even = 0;
      else odd_even = 1;

	  res[j*Tw*3 + i*3] = 255*odd_even;
	  res[j*Tw*3 + i*3 + 1] = 255*odd_even;
	  res[j*Tw*3 + i*3 + 2] = 255*odd_even;
    }
  }
  return res;
}

void panelParamSet(bool redFirst)
{
    LOGD("%s (%d)\n", __FUNCTION__, redFirst);
	m_redFirst = redFirst;
}

void rasterParamSet(float cover, float cot, float offset, bool slope)
{
    LOGD("%s (%f, %f, %f, %d)\n", __FUNCTION__, cover, cot, offset, slope);
    m_cover = cover;
    m_cot = cot;
    m_offset = offset;
    m_slope = slope;
    if (m_slope)
        m_cot = fabs(m_cot);
    else
        m_cot = -fabs(m_cot);
}

bool templateCreate(signed char* buf, int w, int h)
{
    if(m_slope)
    {
        LOGI("slopeTempleteGen");
        slopeTempleteGen((unsigned char* )buf, 6, w, h);
    }
    else
    {
        LOGI("vertTempleteGen");
        vertTempleteGen((unsigned char* )buf, 6, w, h);
    }

    return true;
}
