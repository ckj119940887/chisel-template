#include <stdio.h>
#include <stdint.h>
#include "platform.h"
#include "xil_printf.h"
#include "xil_io.h"
#include "xparameters.h"

int main()
{
    init_platform();

    print("GeneralRegFileToBRAM Test\n\r");

    // write data to shareMem[0]
    Xil_Out32(XPAR_AXIWRAPPERCHISELGENE_0_BASEADDR + 0x0, (uint32_t)(-1));
    // write data to shareMem[1]
    Xil_Out32(XPAR_AXIWRAPPERCHISELGENE_0_BASEADDR + 0x4, (uint32_t)(-2));
    // write data to shareMem[2]
    Xil_Out32(XPAR_AXIWRAPPERCHISELGENE_0_BASEADDR + 0x8, (uint32_t)(-3));
    // write data to shareMem[3]
    Xil_Out32(XPAR_AXIWRAPPERCHISELGENE_0_BASEADDR + 0xC, (uint32_t)(3));

    // write to port valid (generated IP)
    Xil_Out32(XPAR_AXIWRAPPERCHISELGENE_0_BASEADDR + 0x400, 0x1);

    // read from port ready (generated IP)
    uint32_t ready = Xil_In32(XPAR_AXIWRAPPERCHISELGENE_0_BASEADDR + 0x404);
    while (ready != 1)
    {
        ready = Xil_In32(XPAR_AXIWRAPPERCHISELGENE_0_BASEADDR + 0x404);
    }

    // read the final result from BRAM
    uint32_t result = Xil_In32(XPAR_AXIWRAPPERCHISELGENE_0_BASEADDR + 0x10);
    printf("the final result: %d \n\r", (int32_t)result);

    print("Successfully ran application");

    cleanup_platform();
    return 0;
}
