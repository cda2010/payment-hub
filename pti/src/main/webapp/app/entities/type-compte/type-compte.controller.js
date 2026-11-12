(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeCompteController', TypeCompteController);

     TypeCompteController.$inject = ['TableService', 'TypeCompte', '$scope'];

    function TypeCompteController( TableService, TypeCompte, $scope) {
        var vm = this;

        var addItemData = {
            templateUrl: 'app/entities/type-compte/type-compte-dialog.html',
            controller: 'TypeCompteDialogController',
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
                        code: null,
                        libelle:null

                    };
                }
            }
        };

        var editItemData = {
            templateUrl: 'app/entities/type-compte/type-compte-dialog.html',
            controller: 'TypeCompteDialogController',
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
            templateUrl: 'app/entities/type-compte/type-compte-dialog.html',
            controller: 'TypeCompteDialogController',
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
            templateUrl: 'app/entities/type-compte/type-compte-delete-dialog.html',
            controller: 'TypeCompteDeleteController',
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
            entity: TypeCompte,
            dialogData: dialogData,
            entitySearch: TypeCompte,
            entitySearchParams: entitySearchParams,
            entityParams: entityParams
        };
        TableService.init(datas);

    }

})();
