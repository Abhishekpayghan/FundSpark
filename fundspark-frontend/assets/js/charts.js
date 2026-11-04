/* ==========================================================
   FundSpark — Chart helpers (Chart.js)
   ========================================================== */

function fsChartDefaults(){
  const dark = document.documentElement.getAttribute('data-theme')==='dark';
  return { text: dark ? '#94A3B8' : '#64748B', grid: dark ? '#1F2A3D' : '#E2E8F0' };
}

// NEW: destroys any Chart.js instance already attached to this canvas before
// a new one is created on it. Without this, re-rendering the SAME canvas
// (e.g. switching a dropdown and calling loadAnalytics() again) throws
// "Canvas is already in use" and silently stops all code after it.
function fsDestroyChart(ctx){
  const existing = Chart.getChart(ctx);
  if(existing) existing.destroy();
}

function fsLineChart(ctx, labels, datasets){
  fsDestroyChart(ctx);
  const d = fsChartDefaults();
  return new Chart(ctx, {
    type:'line',
    data:{ labels, datasets: datasets.map((ds,i)=>({
      label: ds.label, data: ds.data, borderColor: ds.color, borderWidth:3,
      backgroundColor: ctxFn=>{
        const g = ctxFn.chart.ctx.createLinearGradient(0,0,0,240);
        g.addColorStop(0, ds.color+'33'); g.addColorStop(1, ds.color+'00'); return g;
      },
      fill:true, tension:.4, pointRadius:0, pointHoverRadius:5
    }))},
    options:{
      responsive:true, maintainAspectRatio:false,
      plugins:{ legend:{ display: datasets.length>1, labels:{color:d.text, usePointStyle:true, boxWidth:8} } },
      scales:{
        x:{ grid:{display:false}, ticks:{color:d.text, font:{size:11}} },
        y:{ grid:{color:d.grid}, ticks:{color:d.text, font:{size:11}} }
      }
    }
  });
}

function fsBarChart(ctx, labels, data, color){
  fsDestroyChart(ctx);
  const d = fsChartDefaults();
  return new Chart(ctx, {
    type:'bar',
    data:{ labels, datasets:[{ data, backgroundColor: color||'#2563EB', borderRadius:8, maxBarThickness:34 }]},
    options:{
      responsive:true, maintainAspectRatio:false,
      plugins:{legend:{display:false}},
      scales:{ x:{grid:{display:false}, ticks:{color:d.text,font:{size:11}}}, y:{grid:{color:d.grid}, ticks:{color:d.text,font:{size:11}}} }
    }
  });
}

function fsDoughnut(ctx, labels, data, colors){
  fsDestroyChart(ctx);
  const d = fsChartDefaults();
  return new Chart(ctx, {
    type:'doughnut',
    data:{ labels, datasets:[{ data, backgroundColor: colors, borderWidth:0 }]},
    options:{
      responsive:true, maintainAspectRatio:false, cutout:'68%',
      plugins:{ legend:{ position:'bottom', labels:{color:d.text, usePointStyle:true, boxWidth:8, padding:14, font:{size:11.5}} } }
    }
  });
}