(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReglementTraitementController',ReglementTraitementController);

    ReglementTraitementController.$inject = ['$http','Notification','CancelAlert','Reglement','RequestUtils','$uibModalInstance','entity','$state'];

    function ReglementTraitementController($http,Notification,CancelAlert,Reglement,RequestUtils,$uibModalInstance,entity,$state) {
        var vm = this;

        vm.operation = entity;
        vm.clear=clear;
        vm.confirm=confirm;

        vm.clear();
        function clear() {
            console.log("clear");
            $uibModalInstance.dismiss('cancel');
        }


        function confirm() {
            CancelAlert.confirm(function () {
              
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function () {
                              vm.isSaving=true;
                            console.log("Validation of",vm.operation,vm.operation.id);
                             RequestUtils.OperationReglementCompte.save(vm.operation, function(result){
                                console.log("Operation validée! ",result);
                                $uibModalInstance.close(true);
                                console.log('reload page');
                                $state.reload('reglement-organisation-valide');
                            });
                          /* Reglement.valideReglement({reglementId:vm.operation.id,login:vm.operation.login},function (result) {
                                console.log("Operation validée! ",result);
                                $uibModalInstance.close(true);
                                console.log('reload page');
                                $state.reload('reglement-organisation-valide');
                            });*/
                            $state.go('reglement-organisation-valide');
                        });
                        $state.go('reglement-organisation-valide');
                    }).error(function () {
                    Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                });
            });
        }



    }
})();
