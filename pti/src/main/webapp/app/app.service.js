(function() {
    
    
    'use strict';
angular
    .module('ptiApp')
    .factory('AppService', AppService);
     AppService.$inject = ['$uibModal','$state'];
     
      function AppService ($uibModal,  $state) {
           
            return {
                 showModal: function (varModal) {
                   var modalInstance=  $uibModal.open(varModal);
                       modalInstance.result.then(function (selectedItem) {
                                 $state.go($state.current.name, {}, { reload: true });
                           }, function () {

                       });
                 }
                 /*
                  showToastr: function (msg,type) {
                           switch (type){
                               case "error":
                                     toastr.error(msg, 'Error');
                               break;
                               case "info":   
                                    toastr.info(msg, 'Info');
                               break;
                               case "warning":  
                                    toastr.warning(msg, 'Info');
                               break;
                               case "success":  
                                    toastr.success(msg);
                               break;
                           }
                     
                 }*/
                
            };
      }
    
})();