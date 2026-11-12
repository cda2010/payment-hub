(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilController', ProfilController);

    ProfilController.$inject = ['TableService','$scope','$state', 'Profil','RequestUtils', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function ProfilController(TableService,$scope,$state, Profil,RequestUtils, ParseLinks, AlertService, paginationConstants, pagingParams) {


        var vm = this;

        var addItemData ={
            templateUrl: 'app/entities/profil/profil-dialog.html',
            controller: 'ProfilDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        nom: null,
                        description: null,
                        id: null
                    };
                }
            }
        };

        /**
         *Modification d'un élément
         */
        var editItemData = {
            templateUrl: 'app/entities/profil/profil-dialog.html',
            controller: 'ProfilDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: null
            }
        };

        /**
         * Suppresion d'un élément
         */
        var deleteItemData ={
            templateUrl: 'app/entities/profil/profil-delete-dialog.html',
            controller: 'ProfilDeleteController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                entity: null
            }
        };

        var detailItemData = {
            templateUrl: 'app/entities/pays/pays-detail.html',
            controller: 'PaysDetailController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: null, //valeur déterminé à l'appel de la boite
                isViewing: true
            }
        };

        var dialogData = {
            addData: addItemData,
            editData: editItemData,
            deleteData: deleteItemData,
            detailData: detailItemData
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
            //entity: Profil,
            entity: RequestUtils.ProfilOptimise,
            //entitySearch: Profil,
            entitySearch: RequestUtils.ProfilOptimise,
            dialogData: dialogData,
            entitySearchParams: entitySearchParams,
            entityParams: entityParams
        };
        TableService.init(datas);
    }
})();
