(function(){if(window.nrpInsert)return;
var last=null;
document.addEventListener("selectionchange",function(){var s=getSelection();if(!s.rangeCount)return;var n=s.getRangeAt(0).startContainer;var e=n.nodeType===1?n:n.parentElement;if(e&&e.closest&&e.closest("[contenteditable=\"true\"]"))last=s.getRangeAt(0).cloneRange();});
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

window.nrpInsert=function(text){
if(!text||!text.trim())return "クリップボードが空です。先にMarkdownをコピーしてください。";
var ed=null;if(last){var n=last.startContainer;var e=n.nodeType===1?n:n.parentElement;ed=e&&e.closest("[contenteditable=\"true\"]");}
if(!ed)ed=document.querySelector(".ProseMirror[contenteditable=\"true\"]")||document.querySelector("[contenteditable=\"true\"]");
if(!ed)return "編集欄が見つかりません。下書きを開いて本文をタップしてから押してください。";
var html=md(text);ed.focus();var s=getSelection();s.removeAllRanges();
if(last&&ed.contains(last.startContainer))s.addRange(last);else{var r=document.createRange();r.selectNodeContents(ed);r.collapse(false);s.addRange(r);}
var before=ed.innerHTML;
try{var dt=new DataTransfer();dt.setData("text/html",html);dt.setData("text/plain",text);ed.dispatchEvent(new ClipboardEvent("paste",{clipboardData:dt,bubbles:true,cancelable:true}));}catch(x){}
if(ed.innerHTML!==before)return "OK:流し込みました（貼り付け方式）";
try{document.execCommand("insertHTML",false,html);}catch(x){}
if(ed.innerHTML!==before)return "OK:流し込みました（挿入方式）";
return "流し込めませんでした。";};})();