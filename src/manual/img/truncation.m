clear all
x=linspace(-2,1.5,300);
y=0.10*x-0.10*(x).^2+0.04*(x).^4;
yt=0.01*floor(100*y);
plot(x,yt, x,y )
grid on

fid = fopen('truncation1.dat','w');
fprintf(fid,'%12.8f  %12.8f\n', [x;  y]);
fclose(fid);
fid = fopen('truncation2.dat','w');
fprintf(fid,'%12.8f  %12.8f\n', [x;  yt]);
fclose(fid);
