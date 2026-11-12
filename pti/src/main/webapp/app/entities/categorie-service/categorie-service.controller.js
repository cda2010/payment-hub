(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CategorieServiceController', CategorieServiceController);

        CategorieServiceController.$inject = ['$state','TableService', 'CategorieService','InitCrud','$scope'];

    function CategorieServiceController($state,TableService, CategorieService,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des etapes par services";
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
            templateUrl: 'app/entities/categorie-service/categorie-service-dialog.html',
            controller: 'CategorieServiceDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        code: null,  
                        libelle: null,
                        ordre: null,
                        typeServices:null,
                        id: null
                    };
                }
            }
        };
        /**
         *Modification d'un élément
         */
         var editItemData = {
            templateUrl: 'app/entities/categorie-service/categorie-service-dialog.html',
            controller: 'CategorieServiceDialogController',
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
            templateUrl: 'app/entities/categorie-service/categorie-service-delete-dialog.html',
            controller: 'CategorieServiceDeleteController',
            controllerAs: 'vm',
            size: 'sm',  
            resolve: {
                entity: null
            }
        };
        var dialogData = {
            addData: addItemData,
            editData: editItemData,
            deleteData: deleteItemData,
            //detailData: detailItemData
        }; 
        var datas = {
            vm: vm,
            entityDatas: {
                code: null,  
                libelle: null,
                ordre: null,
                //typeServices:null,
                id: null
            }, 
            entity: CategorieService,
            addTemplateUrl: "categorie-service/categorie-service-dialog.html",
            deleteTemplateUrl: "categorie-service/categorie-service-delete-dialog.html",
            controller: "CategorieService",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
