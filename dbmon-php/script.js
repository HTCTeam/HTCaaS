
function wopen(url, width, height, scrollbars, resizable) {
  option = "width="+width
          +",height="+height
          +",scrollbars="+scrollbars
          +",resizable="+resizable;
          //+",status="+status; 
  open(url, '', option);
}

// wopen with name
function wopen2(url, name, width, height, scrollbars, resizable) {
  option = "width="+width
          +",height="+height
          +",scrollbars="+scrollbars
          +",resizable="+resizable;
          //+",status="+status; 
  open(url, name, option);
}

