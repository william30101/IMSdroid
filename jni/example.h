

class Shape {
public:
	Shape() {
		nshapes++;
	}
	virtual ~Shape() {
		nshapes--;
	};
	double  x, y;
	void    move(double dx, double dy);
	virtual double area(void) = 0;
	virtual double perimeter(void) = 0;
	static  int nshapes;
};

class Circle : public Shape {
	private:

	public:
		double radius;
		Circle(double r) : radius(r) { };
		virtual double area(void);
		virtual double perimeter(void);
};

class Square : public Shape {
	private:
		double width;
	public:
		Square(double w) : width(w) { };
		virtual double area(void);
		virtual double perimeter(void);
};


class Ope {
	public:
		static  int beSendSize;
		static unsigned char beSendData[13];
		virtual void initByteArray();
		virtual int addToByteArray(unsigned char b, int count );
		virtual void printOpeByteArray(void);
		virtual void printByteArray(unsigned char inByte[] , int len);
		virtual unsigned char* ByteArrayToString(void);

};
