var modules={'react-native': require('react-native')};modules['./images/cljs.png']=require('./images/cljs.png');
modules['base-64']=require('base-64');
modules['react']=require('react');
require('figwheel-bridge').withModules(modules).start('CljsrnExample','android','10.0.3.2');