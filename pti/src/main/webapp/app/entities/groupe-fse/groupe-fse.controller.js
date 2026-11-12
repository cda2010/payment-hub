(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupeFseController', GroupeFseController);

        GroupeFseController.$inject = ['$state','TableService', 'GroupeFse','InitCrud','$scope'];

    function GroupeFseController($state,TableService, GroupeFse,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des Groupe Fse";
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
            templateUrl: 'app/entities/groupe-fse/groupe-fse-dialog.html',
            controller: 'GroupeFseDialogController',
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
            templateUrl: 'app/entities/groupe-fse/groupe-fse-dialog.html',
            controller: 'GroupeFseDialogController',
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
            templateUrl: 'app/entities/groupe-fse/groupe-fse-delete-dialog.html',
            controller: 'GroupeFseDeleteController',
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
                libelle: null,
                ordre: null,
                typeServices:null,
                id: null
            }, 
            entity: GroupeFse,
            addTemplateUrl: "groupe-fse/groupe-fse-dialog.html",
            deleteTemplateUrl: "groupe-fse/groupe-fse-delete-dialog.html",
            controller: "GroupeFse",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
