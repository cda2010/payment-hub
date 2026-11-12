(function() {
    "use strict";
    angular.module('ptiApp').service('TableAgentService', ['$uibModal', '_', 'ParseLinks', 'Notification','Client','LigneFichier', 'paginationConstants', service]);

    function service($uibModal, _, ParseLinks, Notification,Client,LigneFichier, paginationConstants) {
        var self = this;
        self.init = init;
        self.initSimpleTable = initSimpleTable;
        self.initParentInDropdown = initParentInDropdown;
        function init(data) {
            var tableScope = data.scope,
                vm = data.vm,
                Entity = data.entity,
                EntitySearch = data.entitySearch,
                dialogData = data.dialogData,
                dialogTest = data.dialogTest,
                entityParams = data.entityParams,
                loadCallBack = data.loadCallBack,
                entitySearchParams = data.entitySearchParams;
            vm.predicate = 'id';
            /*True = ASC et False = DESC*/
            vm.reverse = false;
            // vm.itemsPerPage = paginationConstants.itemsPerPage;
            if (angular.isDefined(data.itemsPerPage)) {
                vm.itemsPerPage = data.itemsPerPage;
            } else {
                vm.itemsPerPage = 20;
            }
            vm.clear = clear;
            vm.search = search;
            vm.loadAll = loadAll;
            vm.setItemsPerPage = setItemsPerPage;
            vm.sortBy = sortBy;
            vm.isCurrentColumn = isCurrentColumn;
            /*Page courante*/
            vm.page = 1;
            /*Nombre d'élements*/
            vm.totalItems = null;
            vm.searchQuery = null;
            vm.currentSearch = null;

            /*Méthode d'ajout, de suppression et de modification*/
            vm.editItem = editItem;
            vm.addItem = addItem;
            vm.deleteItem = deleteItem;
            vm.detailItem = detailItem;
            /*Fonction permettant de n'afficher la pagination que si besoin*/
            vm.canShowPagination = canShowPagination;
            /*Id de la ligne courante sélectionnée*/
            vm.idSelectedCode = null;

            vm.datas = [];
            vm.test = dialogTest;

            vm.forward = function(idCode) {
                /*Initialisation du code selectionné*/
                vm.idSelectedCode = idCode;
                /*Scroll automatique jusqu'au details*/
                //smoothScroll.scrollTo('mf-details');
            };

            function canShowPagination() {
                console.log(vm.totalItems,'et',vm.itemsPerPage);
                return vm.totalItems > 0 && (vm.totalItems > vm.itemsPerPage);
            }

            /*Observe le changement de la valeur du numéro de page*/
            tableScope.$watch(angular.bind(vm, function() {
                return vm.page;
            }), function(newPage) {
                if (!waitForParent()) {
                    loadAll();
                }
            });

            function setItemsPerPage(value) {
                vm.itemsPerPage = value;
                changePageAndLoadAll();
            }
            /**
             *Utilisée au cas où les éléments de l'entité sont listés en fonction de l'id du parent.
             *Dans ce cas, on verifie que l'entité est bien associée à un parent et que ce parent
             *est à l'instant T connu.
             */
            function waitForParent() {
                /*var b  = false ;*/
                if (angular.isFunction(vm.getParentId)) {
                    var id = vm.getParentId();
                    if (angular.isUndefined(id) || id === null) {
                        return true;
                    }
                }

                return false;
            }

            function changePageAndLoadAll() {

                if (vm.page == 1) {
                    loadAll();
                } else {
                    /*Le changement de la page rechargement directement les données du à un watcher sur cet attribut*/
                    vm.page = 1;
                }
            }

            /*  Service pour case à cocher liste client dans lot */
            vm.bool = [];
            vm.ligneChargementFichiers = [];
            vm.listeF = [];

            vm.exist = function(id){
                return vm.bool.indexOf(id) > -1;
            };

            vm.check = function(id){
                var idx = vm.bool.indexOf(id);
                if (idx > -1){
                    vm.bool.splice(idx, 1);
                    vm.listeF.splice(idx, 1);

                }else{
                    vm.bool.push(id);

                    LigneFichier.get({id: id}, function (result) {
                        // console.log(result);
                        vm.listeF.push(result);
                    });
                }
                vm.cocherLaCase(vm.bool.length, vm.ligneChargementFichiers.length);
                console.log("selected", vm.bool);
                console.log("selectedss", vm.cocherLaCase(vm.bool.length, vm.ligneChargementFichiers.length));
            };

            vm.selectAll = function() {

                if(vm.selectAll){
                    angular.forEach(vm.ligneChargementFichiers, function(item) {
                        var idx = vm.clients.indexOf(item.id);
                        if (idx >= 0){
                            return true;
                        }else{
                            vm.bool.push(item.id);
                            vm.listeF.push(item);
                        }
                    });

                }

                vm.unique = Array.from(new Set(vm.bool));
                vm.bool2 = [];
                if(vm.bool.length !== vm.unique.length) {

                    if ((vm.bool.length / vm.unique.length>>0) >0){

                        vm.bool2 = vm.bool ;
                        if ((vm.bool2.length / vm.unique.length>>0) === 1){
                            vm.bool = vm.unique;

                        }else if ((vm.bool2.length / vm.unique.length>>0) > 1){
                            vm.bool = [];
                            vm.listeClients = [];
                            vm.ligneChargementFichiers = [];
                            vm.bool2 = [];
                        }

                    }

                }
            };

            vm.cocherLaCase = function(selectedElt, allElt){
                vm.coche = false;
                if (selectedElt === allElt){
                    vm.coche = true;
                    return  vm.coche;
                }else {
                    vm.coche = false;
                    return vm.coche;
                }
            };


            function loadAll() {
                console.log("***Loading items");
                if (vm.currentSearch !== null) {
                    var d = _.merge({ /*Paramètres initials*/
                        query: vm.currentSearch,
                        page: vm.page - 1,
                        size: vm.itemsPerPage,
                        sort: sort()
                    }, entitySearchParams());
                    EntitySearch.query(d, onSuccess, onError);
                } else {
                    var d = _.merge({
                        page: vm.page - 1,
                        size: vm.itemsPerPage,
                        sort: sort()
                    }, entityParams());
                    Entity.query(d, onSuccess, onError);
                }

                function sort() {
                    var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                    if (vm.predicate !== 'id') {
                        result.push('id');
                    }
                    return result;
                }

                function onSuccess(data, headers) {
                    /**
                     *Cette condition est testée pour pouvoir prendre en compte le faite que
                     *toutes les tables ne prennent pas en compte la pagination.
                     */
                    if (headers('link') !== null && headers('link') !== undefined) {
                        vm.links = ParseLinks.parse(headers('link'));
                        vm.totalItems = headers('X-Total-Count');
                        vm.queryCount = vm.totalItems;
                    } else {
                        vm.queryCount = data.length;
                    }
                    vm.datas = data;
                    if (angular.isFunction(loadCallBack)) {
                        loadCallBack();
                    }

                }

                function onError(error) {
                    if (error.data.message !== "Not Found")
                        Notification.error(error.data.message);
                }
                console.log('vm.nomClient',entityParams);

            }

            function search(searchQuery) {
                if (!searchQuery) {
                    return vm.clear();
                }
                vm.links = null;
                vm.predicate = 'id';
                vm.reverse = false;
                vm.currentSearch = searchQuery;
                vm.idSelectedCode = null;
                changePageAndLoadAll();
            }

            function clear() {
                vm.links = null;
                vm.predicate = 'id';
                vm.reverse = true;
                vm.currentSearch = null;
                vm.searchQuery = null;
                vm.idSelectedCode = null;
                changePageAndLoadAll();
            }

            /*##############################################################################*/
            /*##########################Gestion du trie par colonne#########################*/
            function sortBy(column) {
                if (column === vm.predicate) { //S'il s'agit de la même column, on inverse l'ordre du trie
                    reverse();
                } else { // Sinon, on remet l'ordre à ascendant ou croissant
                    vm.reverse = true;
                }
                vm.predicate = column;
                loadAll();
            }

            /*Inverse l'ordre de trie*/
            function reverse() {
                vm.reverse = !vm.reverse;
            }

            function isCurrentColumn(column) {
                return vm.predicate === column;
            }

            /**
             *Ajout d'un élément
             */
            function addItem() {
                console.log("Add item");
                $uibModal.open(dialogData.addData).result.then(function(result) {
                    Notification.success('Enregistrement reussi !');
                    /*Ajout de l'élément ajouté dans le tableau, en le triant avec les paramètres courants*/
                    if (vm.datas !== null) {
                        vm.totalItems = vm.totalItems + 1;
                        if (vm.itemsPerPage > vm.totalItems) {
                            vm.datas.push(result);
                            vm.datas = _.sortBy(vm.datas, vm.predicate, !vm.reverse);
                            loadAll();
                        } else {
                            loadAll();
                        }
                    } else {
                        console.error("***vm.datas is null");
                    }
                }, function(datas) {
                    if (angular.isDefined(datas) && datas == true) {
                        loadAll();
                    }

                });
            }

            function detailItem($event, item) {
                $event.stopPropagation();
                dialogData.detailData.resolve.entity = angular.copy(item);
                console.log("***detailItem for " + item.id,dialogData.detailData);
                $uibModal.open(dialogData.detailData).result.then(function(datas) {

                    if (datas.isEdit) {
                        angular.forEach(item, function(value, key) {
                            item[key] = datas.result[key];
                        });
                        //    Notification.success('Modification reussit !');
                    }
                    loadAll();
                }, function() {

                    console.info("***Closing detail dialog");
                });
            }

            /**
             *Modification d'un élément
             */
            function editItem($event, item) {
                console.log("edit Item");
                $event.stopPropagation();
                dialogData.editData.resolve.entity = angular.copy(item);
                $uibModal.open(dialogData.editData).result.then(function(result) {
                    Notification.success('Modification reussie !');
                    /*Les valeurs dans result écrase celle d'item */
                    angular.forEach(item, function(value, key) {
                        item[key] = result[key];
                    });
                    loadAll();
                }, function() {

                });
            }

            /**
             *Suppression d'un élément
             **/
            function deleteItem($event, item) {
                console.log('----------'+item);
                $event.stopPropagation();
                dialogData.deleteData.resolve.entity = item;
                $uibModal.open(dialogData.deleteData).result.then(function(result) {
                    loadAll();
                    Notification.success('Suppression effectué !');
                }, function() {

                });
            }
            return vm;
        }

        function initParentInDropdown(data) {
            var vm = data.vm,
                scope = data.scope,
                ParentEntity = data.ParentEntity,
                filter = data.filter,
                orderBy = data.orderBy,
                dialogData = data.dialogData;

            vm.parents = loadParents();
            vm.addParent = addParent;
            vm.editParent = editParent;
            vm.deleteParent = deleteParent;
            vm.getParentId = getParentId;
            vm.currentParent = {};
            vm.setCurrentParent = setCurrentParent;
            vm.hasParents = hasParents;

            /*Observe le changement de la catégorie*/
            scope.$watch(angular.bind(vm, function() {
                if (angular.isDefined(vm.currentParent)) {
                    return vm.currentParent.id;
                }
            }), function(newValue) {
                //console.log('***New value for currentParent id = ' + newValue);
                if (angular.isDefined(newValue)) {
                    //Obtention de l'objet à partir de son id
                    var item = _.find(vm.parents, function(p) {
                        p.id === newValue;
                    });
                    vm.setCurrentParent(item, false);
                    /*Chargement des critères si la catégorie sélectionnée change!*/
                    if (angular.isFunction(vm.loadAll)) {
                        vm.loadAll();
                    }
                }
            });

            function hasParents() {
                if (angular.isDefined(vm.parents)) {
                    return vm.parents.length > 0;
                }
                return false;
            }

            function getParentId() {
                if (angular.isDefined(vm.currentParent)) {
                    return vm.currentParent.id;
                } else {
                    console.error("***Parent not selected.");
                    return null;
                }
            }

            function setCurrentParent(item, eraseId) {
                console.log('***Changing current parent.With eraseId = ' + eraseId);
                angular.forEach(item, function(value, key) {
                    if (eraseId || key != "id") {
                        //console.log("***Copy of key = " + key);
                        //console.log("***Old value = " + vm.currentParent[key]);
                        vm.currentParent[key] = item[key];
                        //console.log("***New value = " + vm.currentParent[key]);
                    }
                });
            }

            function loadParents() {
                return ParentEntity.query(function() {
                    if (angular.isDefined(vm.parents) && vm.parents.length > 0) {
                        vm.parents = filter('orderBy')(vm.parents, orderBy);
                        vm.setCurrentParent(vm.parents[0], true);
                    }
                }, function() {
                    console.error("Impossible de charger la liste des parents.");
                });
            }

            function addParent() {
                console.log('***Add a parent');
                $uibModal.open(dialogData.addParent).result.then(function(result) {
                    if (vm.parents !== null) {
                        vm.parents.push(result);
                        vm.parents = filter('orderBy')(vm.parents, orderBy);
                        vm.setCurrentParent(vm.parents[0], true);
                    } else {
                        console.log("***vm.parents is NULL");
                    }
                }, function() {});
            }

            function editParent() {
                console.log('***Editing parent with id : ' + vm.currentParent.id);
                //var editedItem = angular.copy(vm.currentParent, {});
                dialogData.editParent.resolve.entity = angular.copy(vm.currentParent);
                //console.log(editedItem);

                $uibModal.open(dialogData.editParent).result.then(function(item) {
                    vm.parents = _.reject(vm.parents, ['id', item.id]);
                    vm.parents.push(item);
                    vm.parents = filter('orderBy')(vm.parents, orderBy);
                    vm.setCurrentParent(item, false);
                });
            }

            function deleteParent() {
                console.log('***Delete parent with id : ' + vm.currentParent.id);
                dialogData.deleteParent.resolve.entity = vm.currentParent;
                $uibModal.open(dialogData.deleteParent).result.then(function() {
                    console.log('***Item deleted.');
                    vm.parents = _.reject(vm.parents, ['id', vm.currentParent.id]);
                    vm.parents = filter('orderBy')(vm.parents, orderBy);
                    vm.setCurrentParent(vm.parents[0], true);
                });
            }
            return vm;
        }
        /**Initialise les tableaux simples qui n'ont ni de pagination,ni de trie, ni de barre de recherche.
         *Uniquement l'affichage, la modification et la suppression d'un élément.
         */
        function initSimpleTable(data) {

        }
    }
})();
