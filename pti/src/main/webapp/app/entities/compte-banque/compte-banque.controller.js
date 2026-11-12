(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteBanqueController', CompteBanqueController);

    CompteBanqueController.$inject = ['TableService', 'CompteBanque', '$scope'];

    function CompteBanqueController( TableService, CompteBanque, $scope) {
        var vm = this;

        var addItemData = {
            templateUrl: 'app/entities/compte-banque/compte-banque-dialog.html',
            controller: 'CompteBanqueDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                isViewing:function(){
                    return false;
                },
                entity: function() {
                    return {
                        /*Initialisation des valeurs par défauts de l'entité à créer*/
                        id : null,
                        codeAgence: null,
                        codeBanque: null,
                        deviseId:null

                    };
                }
            }
        };

        var editItemData = {
            templateUrl: 'app/entities/compte-banque/compte-banque-dialog.html',
            controller: 'CompteBanqueDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                isViewing:function(){
                    return false;
                },
                entity: null //valeur déterminé à l'appel de la boite
            }
        };

        var detailItemData = {
            templateUrl: 'app/entities/compte-banque/compte-banque-dialog.html',
            controller: 'CompteBanqueDialogController',
            controllerAs: 'vm',
            size: 'md',
            resolve: {
                isViewing:function(){
                    return true;
                },
                entity: null //valeur déterminé à l'appel de la boite,
            }
        };
        var deleteItemData = {
            templateUrl: 'app/entities/compte-banque/compte-banque-delete-dialog.html',
            controller: 'CompteBanqueDeleteController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                entity: null //valeur déterminé à l'appel de la boite
            }
        };
        var dialogData = {
            addData: addItemData,
            editData: editItemData,
            detailData: detailItemData,
            deleteData: deleteItemData
        };
        var entitySearchParams = function() {
            return {

            };
        };
        var entityParams = function() {
            return {

            };
        };


        var datas = {
            scope: $scope,
            vm: vm,
            entity: CompteBanque,
            dialogData: dialogData,
            entitySearch: CompteBanque,
            entitySearchParams: entitySearchParams,
            entityParams: entityParams
        };
        TableService.init(datas);

    }

})();
