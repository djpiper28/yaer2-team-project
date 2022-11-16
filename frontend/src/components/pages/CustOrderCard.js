import React, { useEffect, useState } from "react";
import OrderItem from "./OrderItem";
import { getViewOrder } from "../api/ApiClient";




const CustOrderCard = ({ tableNo, Status, index, lastTime }) => {


    return (

        <div className="w-full flex-col justify-center">
            <div className="w-full grid grid-cols-2 justify-center border-b-2 border-polar-night-400">
                <div className="text-left">Table number : {tableNo} </div>
                <div className="text-right">{Status} </div>
            </div>
            <div className="text-sm text-right">
                last change time: {lastTime}
            </div>
        </div>

    );
};



export default CustOrderCard;