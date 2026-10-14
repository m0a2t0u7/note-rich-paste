(()=>{
const D=document;
if(D.getElementById('nrp-box')){D.getElementById('nrp-box').remove();return}
const sel=getSelection();
const range=sel.rangeCount?sel.getRangeAt(0).cloneRange():null;
const startEl=range&&(range.startContainer.nodeType===1?range.startContainer:range.startContainer.parentElement);
const ed=(startEl&&startEl.closest('[contenteditable="true"]'))||D.querySelector('.ProseMirror[contenteditable="true"]')||D.querySelector('[contenteditable="true"]');
const esc=s=>s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
const inl=s=>esc(s).replace(/`([^`]+)`/g,'<code>$1</code>').replace(/\*\*([^*]+)\*\*/g,'<strong>$1</strong>').replace(/__([^_]+)__/g,'<strong>$1</strong>').replace(/(^|[^*])\*([^*]+)\*/g,'$1<em>$2</em>').replace(/\[([^\]]+)\]\(([^)\s]+)\)/g,'<a href="$2">$1</a>');
const md=t=>{const L=t.replace(/\r/g,'').split('\n');let o='',i=0;
while(i<L.length){let l=L[i];
if(/^```/.test(l)){let c=[];i++;while(i<L.length&&!/^```/.test(L[i]))c.push(L[i++]);i++;o+='<pre><code>'+esc(c.join('\n'))+'</code></pre>';continue}
let h=l.match(/^(#{1,6})\s+(.*)/);if(h){o+=(h[1].length<=2?'<h2>':'<h3>')+inl(h[2])+(h[1].length<=2?'</h2>':'</h3>');i++;continue}
if(/^\s*\|/.test(l)){let r=[];while(i<L.length&&/^\s*\|/.test(L[i]))r.push(L[i++]);const cells=x=>x.trim().replace(/^\||\|$/g,'').split('|').map(s=>s.trim());const hd=cells(r[0]);o+='<ul>';r.slice(2).forEach(x=>{o+='<li>'+cells(x).map((c,j)=>hd[j]?inl(hd[j])+'：'+inl(c):inl(c)).join('、')+'</li>'});o+='</ul>';continue}
if(/^\s*[-*+]\s+/.test(l)){o+='<ul>';while(i<L.length&&/^\s*[-*+]\s+/.test(L[i]))o+='<li>'+inl(L[i++].replace(/^\s*[-*+]\s+/,''))+'</li>';o+='</ul>';continue}
if(/^\s*\d+[.)]\s+/.test(l)){o+='<ol>';while(i<L.length&&/^\s*\d+[.)]\s+/.test(L[i]))o+='<li>'+inl(L[i++].replace(/^\s*\d+[.)]\s+/,''))+'</li>';o+='</ol>';continue}
if(/^>\s?/.test(l)){let q=[];while(i<L.length&&/^>\s?/.test(L[i]))q.push(inl(L[i++].replace(/^>\s?/,'')));o+='<blockquote><p>'+q.join('<br>')+'</p></blockquote>';continue}
if(/^\s*([-*_])\1\1+\s*$/.test(l)||!l.trim()){i++;continue}
let p=[];while(i<L.length&&L[i].trim()&&!/^(#{1,6}\s|```|>|\s*[-*+]\s|\s*\d+[.)]\s|\s*\|)/.test(L[i]))p.push(inl(L[i++]));o+='<p>'+p.join('<br>')+'</p>'}
return o};
const box=D.createElement('div');box.id='nrp-box';
box.style.cssText='position:fixed;left:8px;right:8px;bottom:8px;z-index:2147483647;background:#fff;color:#222;border:2px solid #1f8a62;border-radius:10px;padding:10px;font:14px sans-serif;box-shadow:0 4px 20px rgba(0,0,0,.3)';
box.innerHTML='<div style="font-weight:bold;margin-bottom:6px">Markdownを貼って「流し込む」</div><textarea style="width:100%;height:150px;box-sizing:border-box;font:13px monospace;border:1px solid #ccc;border-radius:6px;padding:6px"></textarea><div style="display:flex;gap:8px;margin-top:8px"><button data-a="go" style="flex:1;padding:10px;background:#1f8a62;color:#fff;border:0;border-radius:6px;font-weight:bold">流し込む</button><button data-a="x" style="padding:10px;border:1px solid #ccc;background:#fff;border-radius:6px">閉じる</button></div><div data-m style="margin-top:6px;font-size:12px;color:#555">'+(ed?'入れたい位置にカーソルを置いてから呼び出してください':'編集欄が見つかりません。本文を1回タップしてから呼び出してください')+'</div>';
D.body.appendChild(box);
const ta=box.querySelector('textarea'),m=box.querySelector('[data-m]');
box.querySelector('[data-a="x"]').onclick=()=>box.remove();
box.querySelector('[data-a="go"]').onclick=()=>{
if(!ed){m.textContent='編集欄が見つかりません。';return}
const html=md(ta.value);if(!html){m.textContent='Markdownが空です。';return}
const plain=ta.value;
ed.focus();const s=getSelection();s.removeAllRanges();
if(range&&ed.contains(range.startContainer))s.addRange(range);else{const r=D.createRange();r.selectNodeContents(ed);r.collapse(false);s.addRange(r)}
const before=ed.innerHTML;let way='';
try{const dt=new DataTransfer();dt.setData('text/html',html);dt.setData('text/plain',plain);
const ev=new ClipboardEvent('paste',{clipboardData:dt,bubbles:true,cancelable:true});ed.dispatchEvent(ev);
if(ev.defaultPrevented)way='貼り付け方式'}catch(e){}
setTimeout(()=>{
if(ed.innerHTML===before){try{if(D.execCommand('insertHTML',false,html))way='挿入方式'}catch(e){}}
setTimeout(()=>{if(ed.innerHTML!==before){m.style.color='#1f8a62';m.textContent='流し込みました（'+(way||'自動')+'）。見出しになっているか確認してください。';ta.value=''}else{m.style.color='#b4541a';m.textContent='流し込めませんでした。この結果を教えてください。'}},150)},150)};
setTimeout(()=>ta.focus(),50);
})();
