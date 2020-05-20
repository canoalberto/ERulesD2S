__device__ void push(float f, float stack[], int* sp)
{
	stack[(*sp)++] = f;
}

__device__ float pop(float stack[], int* sp)
{
	return stack[--(*sp)];
}

__device__ bool covers(float* expr, int instance, float* instancesData, int numberInstances_A)
{
	int sp, bufp;
	float stack[MAX_STACK];
	float op1, op2;
	int attribute;	
	
	for(sp = 0, bufp = 0; ;) 
	{
		switch((int) expr[bufp])
		{
			case GREATER:
				attribute = (int) expr[bufp+1];
				op1 = instancesData[instance + numberInstances_A * attribute];
				op2 = expr[bufp+2];
				if (op1 > op2)					push(1, stack, &sp);
				else					    	push(0, stack, &sp);
				bufp += 3;
				break;
			case LESS:
				attribute = (int) expr[bufp+1];
				op1 = instancesData[instance + numberInstances_A * attribute];
				op2 = expr[bufp+2];
				if (op1 < op2)					push(1, stack, &sp);
				else					    	push(0, stack, &sp);
				bufp += 3;
				break;
			case _EQ:
				attribute = (int) expr[bufp+1];
				op1 = instancesData[instance + numberInstances_A * attribute];
				op2 = expr[bufp+2];
				if (op1 == op2)					push(1, stack, &sp);
				else							push(0, stack, &sp);
				bufp += 3;
				break;
			case _NEQ:
				attribute = (int) expr[bufp+1];
				op1 = instancesData[instance + numberInstances_A * attribute];
				op2 = expr[bufp+2];
				if (op1 != op2)					push(1, stack, &sp);
				else							push(0, stack, &sp);
				bufp += 3;
				break;
			case AND:
				op1 = pop(stack, &sp);
				op2 = pop(stack, &sp);
				if (op1 * op2 == 1)				push(1, stack, &sp);
				else						    push(0, stack, &sp);
				bufp++;
				break;
			case OR:
				op1 = pop(stack, &sp);
				op2 = pop(stack, &sp);
				if (op1 == 1 || op2 == 1)	    push(1, stack, &sp);
				else				            push(0, stack, &sp);
				bufp++;
				break;
			case NOT:
				op1 = pop(stack, &sp);
				if(op1 == 0)					push(1, stack, &sp);
				if(op1 == 1)					push(0, stack, &sp);
				bufp++;
				break;
			case END_EXPR:
				return pop(stack, &sp) == 1 ? true : false;
		}
	}
}