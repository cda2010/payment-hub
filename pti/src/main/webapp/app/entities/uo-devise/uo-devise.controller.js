(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UoDeviseController', UoDeviseController);

    UoDeviseController.$inject = ['$state', 'UoDevise','$scope','InitCrud'];

    function UoDeviseController($state, UoDevise,$scope,InitCrud) {

        var vm = this;
        vm.title = "Liste des devises par pays";
        vm.columns = [{
            name: 'libelleUniteOrganisation',
            title: 'Pays'
        },{
            name: 'deviseLibelle',
            title: 'Devise'
        },{
            name: 'defaultDevise',
            title: 'Utilise par defaut?',
            type:"Boolean"
        }];
        
        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {
                code: null,
                libelle: null,
                id: null
            },
            entity: UoDevise,
            addTemplateUrl: "uo-devise/uo-devise-dialog.html",
            deleteTemplateUrl: "uo-devise/uo-devise-delete-dialog.html",
            controller: "UoDevise",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();