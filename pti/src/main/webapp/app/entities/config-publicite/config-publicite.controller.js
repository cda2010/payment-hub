(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ConfigPubliciteController', ConfigPubliciteController);

        ConfigPubliciteController.$inject = ['$state','TableService', 'ConfigPublicite','InitCrud','$scope'];

    function ConfigPubliciteController($state,TableService, ConfigPublicite,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des etapes par services";
        // api/publicite/image/:id
        vm.getImageUrl = function(id) {
            return id ? 'api/publicite/image/' + id : '#';
        };
        vm.create = "Créer ou Modifier"
        vm.columns = [
            {
                name: 'typeCanal',
                title: 'Type Canal'
            },
            {
            name: 'application',
            title: 'Application'
            },  
        ];
        vm.actions = {
            view: true,
            edit: true,
            delete: true
        };
        vm.getRandomColor = function() {
            var badgeClasses = ['primary','secondary','success','danger','warning','info','dark'];
            return badgeClasses[Math.floor(Math.random()*badgeClasses.length)];
        };

        var addItemData = {
            templateUrl: 'app/entities/config-publicite/config-publicite-dialog.html',
            controller: 'ConfigPubliciteDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        pays: null,
                        id: null,
                        frequence:null,
                        dateDebut: null,
                        dateFin:null,
                        urlMinioPublicite:null
                                
                    };
                }
            }
        };
        /**
         *Modification d'un élément
         */
         var editItemData = {
            templateUrl: 'app/entities/config-publicite/config-publicite-dialog.html',
            controller: 'ConfigPubliciteDialogController',
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
        var deleteItemData = {
            templateUrl: 'app/entities/config-publicite/config-publicite-delete-dialog.html',
            controller: 'ConfigPubliciteDeleteController',
            controllerAs: 'vm',
            size: 'sm',  
            resolve: {
                entity: null
            }
        };
        var dialogData = {
            addData: addItemData,
          //  editData: editItemData,
            deleteData: deleteItemData,
            //detailData: detailItemData
        }; 

        /*var datas = {
            scope: $scope,
            vm: vm,
            entity: EtapeService,
            entitySearch: EtapeService,
            dialogData: dialogData,
            entitySearchParamsItem: {},
            entityParamsItem: {}
        };
        TableService.init(datas);*/

        var datas = {
            vm: vm,
            entityDatas: {
                pays: null,
                id: null,
                frequence:null,
                dateDebut: null,
                dateFin:null,
                urlMinioPublicite:null
                                
            }, 
            entity: ConfigPublicite,
            addTemplateUrl: "config-publicite/config-publicite-dialog.html",
            deleteTemplateUrl: "config-publicite/config-publicite-delete-dialog.html",
            controller: "ConfigPublicite",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
