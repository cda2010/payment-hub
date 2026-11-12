(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceFseController', TypeServiceFseController);

        TypeServiceFseController.$inject = ['$state','TableService', 'TypeServiceFse','InitCrud','$scope'];

    function TypeServiceFseController($state,TableService, TypeServiceFse,InitCrud,$scope) {
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
        vm.selectRow = function (uniteOrganisation) {
            vm.selectedRow = TypeServiceFse.queryByUO({id:uniteOrganisation});
            console.log("++++++++++++++++++++++++++++++"+vm.selectedRow.lenght);
        };
        var addItemData = {
            templateUrl: 'app/entities/type-service-fse/type-service-fse-dialog.html',
            controller: 'TypeServiceFseDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        uniteOrganisation:{
                            id: null
                        },
                        typeService: null,
                        id: null
                    };
                }
            }
        };
        /**
         *Modification d'un élément
         */
         var editItemData = {
            templateUrl: 'app/entities/type-service-fse/type-service-fse-dialog.html',
            controller: 'TypeServiceFseDialogController',
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
            templateUrl: 'app/entities/type-service-fse/type-service-fse-delete-dialog.html',
            controller: 'TypeServiceFseDeleteController',
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


        var datas = {
            vm: vm,
            entityDatas: {
                code: null,
                id: null
            }, 
            entity: TypeServiceFse,
            addTemplateUrl: "type-service-fse/type-service-fse-dialog.html",
            deleteTemplateUrl: "type-service-fse/type-service-fse-delete-dialog.html",
            controller: "TypeServiceFse",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
