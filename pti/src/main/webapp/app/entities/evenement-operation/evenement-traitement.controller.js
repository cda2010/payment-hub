(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EvenementTraitementController',EvenementTraitementController);

    EvenementTraitementController.$inject = ['$http','Notification','CancelAlert','RequestUtils','$uibModalInstance','$uibModal','entity','$state'];

    function EvenementTraitementController($http,Notification,CancelAlert,RequestUtils,$uibModalInstance,$uibModal,entity,$state) {
        var vm = this;

        vm.operation = entity;
        vm.clear=clear;
        vm.confirm=confirm;
        //vm.addCommentaire=addCommentaire;
        vm.operation.statut="Non";
        console.log('Value',vm.operation);
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

                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function () {
                            vm.operation.login=vm.username;
                            vm.operation.motif=vm.operation.commentaire;
                            console.log("Validation",vm.operation);
                            RequestUtils.EvenementTraitement.save(vm.operation, function(){
                            console.log("Operation validée! ");
                            
                            
                              console.log(vm.operation);
                              
                              
                            $uibModalInstance.close(true);
                            $state.reload('evenement-operation' );
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
