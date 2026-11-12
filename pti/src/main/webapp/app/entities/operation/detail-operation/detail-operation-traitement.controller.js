(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailOperationTraitementController',DetailOperationTraitementController);

    DetailOperationTraitementController.$inject = ['$http','Notification','CancelAlert','RequestUtils','$uibModalInstance','$uibModal','entity','frais','$state'];

    function DetailOperationTraitementController($http,Notification,CancelAlert,RequestUtils,$uibModalInstance,$uibModal,entity,frais,$state) {
        var vm = this;

        vm.operation = entity;
        vm.typeFrais=frais;

        vm.clear=clear;
        vm.confirm=confirm;
        vm.annulationOperationAvecFrais=annulationOperationAvecFrais;
        vm.annulationOperationSansFrais=annulationOperationSansFrais;


        vm.clear();
        function clear() {
            console.log("clear");
            $uibModalInstance.dismiss('cancel');
        }
        console.log('view op',vm.operation);


         function annulationOperationAvecFrais (avecFrais) {
                RequestUtils.AnnulationAccountAvecRetourFrais.get({
                    operationId: vm.operation.id,
                    avecFrais: avecFrais
                }, function (datas) {
                    if (datas != null) {
                        console.log('redressement s frais',datas);
                        vm.operation = datas;
                        $uibModalInstance.close(true);
                        Notification.success("Remboursement effectué avec succès!");
                        $state.reload("operation");

                    }
                });
        }


         function annulationOperationSansFrais (avecFrais) {
                RequestUtils.AnnulationAccountAvecRetourFrais.get({
                    operationId: vm.operation.id, avecFrais: avecFrais
                }, function (datas) {
                    if (datas != null) {
                        console.log('redressement av frais',datas);
                        vm.operation = datas;
                        $uibModalInstance.close(true);
                        Notification.success("Remboursement effectué avec succès!");
                        $state.reload("operation");
                    }
                });
        }




        function confirm() {
            CancelAlert.confirm(function () {
                vm.isSaving=true;
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function () {

                           
                                console.log('avecFrais',vm.typeFrais);
                                annulationOperationAvecFrais(vm.typeFrais);
                            
                        });
                    }).error(function () {
                    Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                });
            });
        }


        function confirmd(){
            CancelAlert.confirm(function(){
                vm.operationEnCours.operationTraiteId=vm.operation.id;


            });
        }




    }
})();
