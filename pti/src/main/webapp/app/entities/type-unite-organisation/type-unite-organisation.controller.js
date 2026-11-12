(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeUniteOrganisationController', TypeUniteOrganisationController);


    TypeUniteOrganisationController.$inject = ['$scope','InitCrud','$state', 'TypeUniteOrganisation','RequestUtils', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TypeUniteOrganisationController($scope,InitCrud,$state, TypeUniteOrganisation,RequestUtils, ParseLinks, AlertService, paginationConstants, pagingParams) {
        var vm = this;
        vm.title="Liste des types d'organisations";

        vm.columns = [{
            name: 'libelleTypeUO',
            title: 'code'
        },{
            name: 'codeTypeUO',
            title: 'libelle'
        },{
            name: 'niveau',
            title: 'Niveau'
        }];

        vm.actions={
            view:false,
            edit:true,
            delete:true,
            add:true
        };

        var datas={
            vm:vm,
            entityDatas:{
                libelleTypeUO: null,
                codeTypeUO: null,
                niveau: null,
                id: null
            },
            //entity:TypeUniteOrganisation,
            entity:RequestUtils.ListUiniteOrganisationOptimise,
            addTemplateUrl:"type-unite-organisation/type-unite-organisation-dialog.html",
            deleteTemplateUrl:"type-unite-organisation/type-unite-organisation-delete-dialog.html",
            controller:"TypeUniteOrganisation",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"lg",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();
