(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationTraitementController',OperationTraitementController);

        OperationTraitementController.$inject = ['$scope','$http','Notification','CancelAlert','RequestUtils','$uibModalInstance','$uibModal','entity','$state'];

    function OperationTraitementController($scope,$http,Notification,CancelAlert,RequestUtils,$uibModalInstance,$uibModal,entity,$state) {
        var vm = this;

        vm.operation = entity;
        vm.clear=clear;
        vm.confirm=confirm;
        //vm.addCommentaire=addCommentaire;
        vm.operation.statut=null;

        vm.clear();
        function clear() {
            console.log("clear");
            $uibModalInstance.dismiss('cancel');
        }
        console.log('view op',vm.operation);

        vm.addCommentaire= function () {

            $uibModal.open({
                templateUrl: 'app/entities/operation/commentaire/commentaire.html',
                controller: 'CommentaireController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return vm.operation;
                    }
                }
            }).result.then(function () {

                $uibModalInstance.close(true);

            });

        };


 function confirm() {
            CancelAlert.confirm(function () {
                vm.isSaving=true;
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                  vm.operationEnCours.operationTraiteId=vm.operation.id;
                vm.operationEnCours.canalLibelleCanal="WEB";
                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function () {

                         //   vm.operation.uoAcquereurId = vm.fseId;
                            //    vm.operation.code = "CREATION_MONNAIE";
                            
                           vm.operationEnCours.canalLibelleCanal="WEB";
                            console.log("Validation",vm.operationEnCours);
                            RequestUtils.OperationTraitement.save(vm.operationEnCours, function(){
                            console.log("Operation validée! ");
                            $uibModalInstance.close(true);
                              $scope.$emit('ptiApp:operationUpdate', vm.operationEnCours);
                              
                                      console.log("Validation..................");
                            $state.reload('operation' );
                                $state.go('operation',{}, {reload: true});
                  
                });
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
