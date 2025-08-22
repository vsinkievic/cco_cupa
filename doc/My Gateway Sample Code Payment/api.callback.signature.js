//************************************//
//     api.callback.signature.js      //
//                                    //
// Example of verification of the     //
// signature in a callback            //
//                                    //
// dev@ss.net                         //
//                                    //
// Ver 6.x                            //
// (c) My Gateway 2019 - 2024         //
//************************************//

//*************************************************************************
// The crypto library being used here for MD5 implementation
//
// "https://cdn.jsdelivr.net/npm/crypto-js@3.1.9-1/crypto-js.min.js"
// "https://cdn.jsdelivr.net/npm/crypto-js@3.1.9-1/sha256.js"
//*************************************************************************

//    The callback parameters are passed in the URL
//    www.callback.html?currency={}&success={Y/N}&merchantID={MID}&orderID={Order}&clientID={Client}&amount={XX.xx}&signature={Singature}

function verify_callback() {
  //    Get the URL parameters

  var queryString = window.location.search;
  var urlParams = new URLSearchParams(queryString);

  //  Merchant Key as provided by your processor

  var merchantKey = '1234567890';

  //    Get the URL parameters, if they are
  //    not there replace with an empty string

  var success = urlParams.get('success') ? urlParams.get('success') : '';
  var currency = urlParams.get('currency') ? urlParams.get('currency') : '';
  var clientID = urlParams.get('clientID') ? urlParams.get('clientID') : '';
  var amount = urlParams.get('amount') ? urlParams.get('amount') : '';
  var orderID = urlParams.get('orderID') ? urlParams.get('orderID') : '';

  var merchantMID = urlParams.get('merchantID');
  var signature = urlParams.get('signature');

  //    If there's no Merchant ID or signature - abort

  if (!merchantMID || !signature) {
    alert('Merchant ID or Signature not found');
    return false;
  }

  //**************************************
  // Check the signature of the response
  //**************************************

  //    Create the Crypto Signature of the payment data
  //    using the merchant key

  var clearTxt = success + clientID + orderID.toLowerCase() + CryptoJS.MD5(merchantKey).toString() + amount + currency + merchantMID;

  var theSignature = CryptoJS.MD5(clearTxt).toString();

  // Check the Signature is correct

  if (theSignature != signature) {
    alert('Signature verification failed!');
    return false;
  } else {
    alert('Signature verification success!');
    return true;
  }
}
